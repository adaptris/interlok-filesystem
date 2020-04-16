package com.adaptris.filesystem.smbj;

import static com.adaptris.core.CoreConstants.FS_CONSUME_DIRECTORY;
import static com.adaptris.core.CoreConstants.FS_FILE_SIZE;
import static com.adaptris.core.CoreConstants.ORIGINAL_NAME_KEY;
import static com.hierynomus.protocol.commons.EnumWithValue.EnumUtils.isSet;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.ftp.FtpConsumer;
import com.adaptris.interlok.cloud.RemoteFile;
import com.adaptris.interlok.util.Args;
import com.adaptris.interlok.util.FileFilterBuilder;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consume from a SMB share, deleting the file after consumption.
 * <p>
 * For consistency with the other 'filesystem' style consumers (such as {@link FsConsumer} and {@link FtpConsumer} this will provide
 * the same metadata keys containing the original file name ({@value com.adaptris.core.CoreConstants#ORIGINAL_NAME_KEY}); the
 * directory ( {@value com.adaptris.core.CoreConstants#FS_CONSUME_DIRECTORY}) - in this instance the directory will be UNC path so
 * it will include the server/share-name; and the filesize ({@value com.adaptris.core.CoreConstants#FS_FILE_SIZE}).
 * </p>
 * 
 * <p>
 * The behaviour of this consumer is to not recurse into sub-directories and to ignore hidden files.
 * </p>
 * 
 * @config smb-consumer
 * 
 */
@XStreamAlias("smb-consumer")
@ComponentProfile(summary = "Consume from an SMB location", tag = "consumer,samba,smb", recommended = {SMBConnection.class},
    since = "3.10.1", 
    metadata = {CoreConstants.ORIGINAL_NAME_KEY, CoreConstants.FS_FILE_SIZE, CoreConstants.FS_CONSUME_DIRECTORY,
        CoreConstants.MESSAGE_CONSUME_LOCATION})
@DisplayOrder(order = {"connection", "fileFilterImp", "poller"})
@NoArgsConstructor
public class SMBConsumer extends AdaptrisPollingConsumer {
  public static final String DEFAULT_FILE_FILTER_IMPL = "org.apache.commons.io.filefilter.RegexFileFilter";

  //
  // Files which have these attributes, we shall be ignoring.
  // Might be arguable that people may well write files to a smb share that are hidden; but if they're hidden
  // we shouldn't be processing them!
  private static final EnumSet<FileAttributes> IGNORED_FILES =
      EnumSet.of(FileAttributes.FILE_ATTRIBUTE_DIRECTORY, FileAttributes.FILE_ATTRIBUTE_HIDDEN);

  /**
   * Set the filename filter implementation that will be used for filtering files.
   * <p>
   * The expression that is used to filter messages is obtained from the associated {@link ConsumeDestination}, if not specified,
   * then the default is {@code org.apache.commons.io.filefilter.RegexFileFilter} which uses the {@code java.util} regular
   * expressions to perform filtering.
   * </p>
   * <p>
   * Note that because we are working against a remote server, support for additional file attributes such as size (e.g. via
   * {@link com.adaptris.core.fs.SizeGreaterThan}) or last modified may not be supported.
   * </p>
   *
   * @see com.adaptris.core.ConsumeDestination#getFilterExpression()
   */
  @InputFieldHint(ofType = "java.io.FileFilter")
  @AdvancedConfig
  @Getter
  @Setter
  private String fileFilterImp;

  // Always non-null because FileFilterBuilder does that
  protected transient FileFilter fileFilter;

  @Override
  protected void prepareConsumer() throws CoreException {
  }

  @Override
  public void init() throws CoreException {
    Args.notNull(getDestination(), "destination");
    fileFilter = FileFilterBuilder.build(getDestination().getFilterExpression(), fileFilterImp());
  }

  @Override
  protected int processMessages() {
    int count = 0;
    SmbPath smbPath = SmbPath.parse(getDestination().getDestination());
    SMBConnection conn = retrieveConnection(SMBConnection.class);
    try {
      Connector worker = conn.createOrGetWorker(smbPath);
      List<RemoteFile> files = list(worker.getDiskShare(), smbPath);
      for (RemoteFile file : files) {
        AdaptrisMessage msg = decode(buildMessage(worker.getDiskShare(), file));
        retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
        count++;
        // Delete the file quietly.
        deleteQuietly(worker.getDiskShare(), SmbPath.parse(file.getPath()));
        if (!continueProcessingMessages(count)) {
          break;
        }
      }
    } catch (Exception e) {
      log.warn("Failed to poll [{}] hoping for success next poll time", smbPath.toUncPath(), e);
    }
    return count;
  }

  @Override
  public String consumeLocationKey() {
    return FS_CONSUME_DIRECTORY;
  }

  private void deleteQuietly(DiskShare share, SmbPath smbRef) {
    try {
      share.rm(smbRef.getPath());
    } catch (Exception e) {
      log.warn("Failed to delete [{}]", smbRef);
    }
  }

  private AdaptrisMessage buildMessage(DiskShare share, RemoteFile fileRef) throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.defaultIfNull(getMessageFactory()).newMessage();
    log.trace("Attempting to get [{}], estimated size [{}]", fileRef.getPath(), fileRef.length());
    SmbPath smbRef = SmbPath.parse(fileRef.getPath());
    try (
        File smbFile = share.openFile(smbRef.getPath(), EnumSet.of(AccessMask.FILE_READ_DATA), null, SMB2ShareAccess.ALL,
        SMB2CreateDisposition.FILE_OPEN, null); 
        InputStream in = smbFile.getInputStream();
        OutputStream out = msg.getOutputStream()) {
      IOUtils.copy(in, out);
    }
    msg.addMetadata(ORIGINAL_NAME_KEY, fileRef.getName());
    msg.addMetadata(FS_CONSUME_DIRECTORY, fileRef.getParent());
    msg.addMetadata(FS_FILE_SIZE, "" + msg.getSize());
    return msg;
  }

  private List<RemoteFile> list(DiskShare share, SmbPath smbPath) throws Exception {
    log.trace("Listing files in [{}]", smbPath);
    List<FileIdBothDirectoryInformation> files = share.list(smbPath.getPath());
    // discard all the "directories"
    // The "end of file" (after trial and error, is the size) I found it finally documented here
    // https://msdn.microsoft.com/en-us/library/cc232088.aspx
    // do the filtering from the FileFilter
    return files.stream()
        .filter((f) -> canProcess(f.getFileAttributes()))
        .map((f) -> new RemoteFile.Builder().setLastModified(f.getLastAccessTime().toEpochMillis())
            .setPath(new SmbPath(smbPath, f.getFileName()).toUncPath())
            .setIsDirectory(false)
            .setIsFile(true)
            .setLength(f.getEndOfFile()).build())
        .filter((f) -> fileFilter.accept(f))
        .collect(Collectors.toList());
  }

  private boolean canProcess(long fileAttributes) {
    Optional<FileAttributes> result = IGNORED_FILES.stream().filter((e) -> isSet(fileAttributes, e)).findAny();
    return !result.isPresent();
  }

  protected String fileFilterImp() {
    return StringUtils.defaultIfBlank(getFileFilterImp(), DEFAULT_FILE_FILTER_IMPL);
  }

  private AdaptrisMessage decode(AdaptrisMessage raw) throws Exception {
    if (getEncoder() != null) {
      AdaptrisMessageEncoder encoder = getEncoder();
      log.trace("Using {} to decode", encoder.getClass().getName());
      encoder.registerMessageFactory(raw.getFactory());
      try (InputStream in = raw.getInputStream()) {
        return encoder.readMessage(in);
      }
    }
    return raw;
  }

  public <T extends SMBConsumer> T withFileFilterImp(String s) {
    setFileFilterImp(s);
    return (T) this;
  }

}

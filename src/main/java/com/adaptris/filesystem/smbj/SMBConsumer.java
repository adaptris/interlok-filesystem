/*******************************************************************************
 * Copyright 2020 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.filesystem.smbj;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.ftp.FtpConsumer;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.interlok.cloud.RemoteFile;
import com.adaptris.interlok.util.FileFilterBuilder;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.adaptris.core.CoreConstants.FS_CONSUME_DIRECTORY;
import static com.adaptris.core.CoreConstants.FS_FILE_SIZE;
import static com.adaptris.core.CoreConstants.ORIGINAL_NAME_KEY;
import static com.hierynomus.protocol.commons.EnumWithValue.EnumUtils.isSet;

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
   * The expression that is used to filter messages is obtained from the {@code fileFilter}, if not specified,
   * then the default is {@code org.apache.commons.io.filefilter.RegexFileFilter} which uses the {@code java.util} regular
   * expressions to perform filtering.
   * </p>
   * <p>
   * Note that because we are working against a remote server, support for additional file attributes such as size (e.g. via
   * {@link com.adaptris.core.fs.SizeGreaterThan}) or last modified may not be supported.
   * </p>
   *
   */
  @InputFieldHint(ofType = "java.io.FileFilter")
  @AdvancedConfig
  @Getter
  @Setter
  private String fileFilterImp;

  /**
   * The SMB Path to read files from in the form {@code \\server-name\shareName\path\to\dir}.
   *
   */
  @Getter
  @Setter
  @NotBlank
  private String path;
  /**
   * The filter expression to use when listing files.
   * <p>
   * If not specified then will default in a file filter that matches all files.
   * </p>
   */
  @Getter
  @Setter
  private String filterExpression;

  // Always non-null because FileFilterBuilder does that
  protected transient FileFilter fileFilter;

  @Override
  protected void prepareConsumer() throws CoreException {
  }

  @Override
  public void init() throws CoreException {
    fileFilter = FileFilterBuilder.build(filterExpression(), fileFilterImp());
  }

  private String smbPath() {
    return getPath();
  }

  private String filterExpression() {
    return getFilterExpression();
  }

  @Override
  protected int processMessages() {
    int count = 0;
    SmbPath smbPath = SmbPath.parse(smbPath());
    SMBConnection conn = retrieveConnection(SMBConnection.class);
    try {
      Connector worker = conn.createOrGetWorker(smbPath);
      List<RemoteFile> files = list(worker.getDiskShare(), smbPath);
      for (RemoteFile file : files) {
        AdaptrisMessage msg = decode(buildMessage(worker.getDiskShare(), file));
        retrieveAdaptrisMessageListener().onAdaptrisMessage(msg, (callback) -> {
          deleteQuietly(worker.getDiskShare(), SmbPath.parse(file.getPath()));
        });
        count++;
        // Delete the file quietly.
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
    Helper.read(share, smbRef, msg);
    msg.addMetadata(ORIGINAL_NAME_KEY, fileRef.getName());
    msg.addMetadata(FS_CONSUME_DIRECTORY, toUnc(fileRef.getParent()));
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
            .setPath(slasher(new SmbPath(smbPath, f.getFileName()).toUncPath()))
            .setIsDirectory(false)
            .setIsFile(true)
            .setLength(f.getEndOfFile()).build())
        .filter((f) -> fileFilter.accept(f))
        .collect(Collectors.toList());
  }

  // Since SmbPath changes most things into UNC style (i.e. with backslashes)
  // We can't have that, since this will cause issues with RemoteFile in the sense
  // that it won't detect the paths properly if we're executing on Unix.
  // So, we convert all the backslashses back to slashes... SmbPath already
  // does the reverse of this under the covers.
  private static final String slasher(String uncPath) {
    return uncPath.replace('\\', '/');
  }

  private static final String toUnc(String path) {
    return SmbPath.parse(path).toUncPath();
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

  public SMBConsumer withPath(String s) {
    setPath(s);
    return this;
  }

  @Override
  protected String newThreadName() {
    return DestinationHelper.threadName(retrieveAdaptrisMessageListener());
  }

}

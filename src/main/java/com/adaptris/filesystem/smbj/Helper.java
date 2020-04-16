package com.adaptris.filesystem.smbj;

import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN;
import static com.hierynomus.mssmb2.SMB2ShareAccess.ALL;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import org.apache.commons.compress.utils.IOUtils;
import com.adaptris.core.AdaptrisMessage;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Helper {

  private static EnumSet<AccessMask> READ_ACCESS = EnumSet.of(AccessMask.FILE_READ_DATA);
  private static EnumSet<AccessMask> RW_ACCESS = EnumSet.of(AccessMask.GENERIC_ALL);

  /**
   * Read a file into an AdaptrisMessage.
   * 
   */
  public static void read(DiskShare share, SmbPath smbRef, AdaptrisMessage msg) throws Exception {
    try (File f = share.openFile(smbRef.getPath(), READ_ACCESS, null, ALL, FILE_OPEN, null);
        InputStream in = f.getInputStream();
        OutputStream out = msg.getOutputStream()) {
      IOUtils.copy(in, out);
    }
  }

  /**
   * Open a file.
   * 
   */
  public static File open(DiskShare share, SmbPath smbRef, WriteMode mode) {
    return share.openFile(smbRef.getPath(), RW_ACCESS, null, ALL, mode.fileOpenMode(), null);
  }
}

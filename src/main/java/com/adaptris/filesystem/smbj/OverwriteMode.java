package com.adaptris.filesystem.smbj;

import java.io.OutputStream;
import com.adaptris.annotation.ComponentProfile;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.smbj.share.File;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * 
 * Mode which appends to an existing file if it exists.
 * 
 * <p>
 * Uses {@code SMB2CreateDisposition#FILE_OVERWRITE_IF} to open the file.
 * </p>
 * 
 * @config smb-overwrite-mode
 */
@XStreamAlias("smb-overwrite-mode")
@NoArgsConstructor
@ComponentProfile(summary = "Open the (or create a new) file and overwrite it")
public class OverwriteMode implements WriteMode {

  @Override
  public SMB2CreateDisposition fileOpenMode() {
    return SMB2CreateDisposition.FILE_OVERWRITE_IF;
  }

  @Override
  public OutputStream getOutputStream(File smbFile) {
    return smbFile.getOutputStream(false);
  }

}

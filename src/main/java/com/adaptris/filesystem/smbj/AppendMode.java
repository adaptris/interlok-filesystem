package com.adaptris.filesystem.smbj;

import java.io.OutputStream;
import com.adaptris.annotation.ComponentProfile;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.smbj.share.File;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * 
 * Mode which overwrites if the file already exists
 * 
 * <p>
 * Uses {@code SMB2CreateDisposition#FILE_OPEN_IF}.
 * </p>
 * 
 * @config smb-append-mode
 */
@XStreamAlias("smb-append-mode")
@NoArgsConstructor
@ComponentProfile(summary = "Open the (or create a new) file and append to it")
public class AppendMode implements WriteMode {

  @Override
  public SMB2CreateDisposition fileOpenMode() {
    return SMB2CreateDisposition.FILE_OPEN_IF;
  }

  @Override
  public OutputStream getOutputStream(File smbFile) {
    return smbFile.getOutputStream(true);
  }

}

package com.adaptris.filesystem.smbj;

import java.io.OutputStream;
import com.adaptris.annotation.ComponentProfile;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.smbj.share.File;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * The default write mode which assumes that a new file will be created.
 * 
 * <p>
 * Uses {@code SMB2CreateDisposition#FILE_CREATE} which means that if the file already exists, the operations will fail.
 * </p>
 *
 * @config smb-create-mode
 */
@XStreamAlias("smb-create-mode")
@ComponentProfile(summary = "Create the file, failing if it already exists")
public class CreateMode implements WriteMode {

  @Override
  public SMB2CreateDisposition fileOpenMode() {
    return SMB2CreateDisposition.FILE_CREATE;
  }

  @Override
  public OutputStream getOutputStream(File smbFile) {
    return smbFile.getOutputStream(false);
  }

}

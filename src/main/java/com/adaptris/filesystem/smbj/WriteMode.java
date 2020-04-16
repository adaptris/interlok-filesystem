package com.adaptris.filesystem.smbj;

import java.io.OutputStream;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.smbj.share.File;

public interface WriteMode {

  /**
   * What's the mode for opening the file.
   * 
   * @return one of (probably) FILE_CREATE, FILE_OPEN_IF, FILE_OVERWRITE_IT depend on the type of operation you want to do.
   */
  SMB2CreateDisposition fileOpenMode();

  OutputStream getOutputStream(File smbFile);
}

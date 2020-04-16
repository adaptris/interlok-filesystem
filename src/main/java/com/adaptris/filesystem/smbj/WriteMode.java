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

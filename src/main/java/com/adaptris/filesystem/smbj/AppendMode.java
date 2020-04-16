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

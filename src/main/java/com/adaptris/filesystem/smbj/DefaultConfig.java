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

import com.adaptris.annotation.ComponentProfile;
import com.hierynomus.smbj.SmbConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Default configuration for SMB share.
 * 
 * @config smb-default-config
 *
 */
@XStreamAlias("smb-default-config")
@NoArgsConstructor
@ComponentProfile(summary = "Default SMB configuration; probably acceptable in most cases")
public class DefaultConfig implements ConfigProvider {

  @Override
  public SmbConfig smbConfig() throws Exception {
    return SmbConfig.createDefaultConfig();
  }

}

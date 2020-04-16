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
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Guest access to the SMB Share
 * 
 * @config smb-guest-access
 */
@XStreamAlias("smb-guest-access")
@NoArgsConstructor
@ComponentProfile(summary = "'Guest' access to a SMB share")
public class GuestAccess implements AuthenticationProvider {

  @Override
  public AuthenticationContext authContext() {
    return AuthenticationContext.guest();
  }

}

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

import java.util.Optional;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Access to an SMB share via the named user/password/domain.
 * 
 * @config smb-user-access
 */
@DisplayOrder(order = {"domain", "username", "password"})
@XStreamAlias("smb-user-access")
@NoArgsConstructor
@ComponentProfile(summary = "Domain/User/Password access to a SMB share")
public class UserAccess implements AuthenticationProvider {

  @Getter
  @Setter
  private String username;

  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  private String password;

  @Getter
  @Setter
  private String domain;

  @Override
  public AuthenticationContext authContext() throws Exception {
    Optional<String> pw = Optional.ofNullable(Password.decode(ExternalResolver.resolve(getPassword())));
    return new AuthenticationContext(getUsername(), pw.map((p) -> p.toCharArray()).orElse(new char[0]), getDomain());
  }

  public UserAccess withUser(String s) {
    setUsername(s);
    return this;
  }

  public UserAccess withPassword(String s) {
    setPassword(s);
    return this;
  }

  public UserAccess withDomain(String s) {
    setDomain(s);
    return this;
  }
}

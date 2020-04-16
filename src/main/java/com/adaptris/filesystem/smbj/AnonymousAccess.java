package com.adaptris.filesystem.smbj;

import com.adaptris.annotation.ComponentProfile;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Anonymous access to a SMB share.
 * 
 * @config smb-anonymous-access
 *
 */
@XStreamAlias("smb-anonymous-access")
@NoArgsConstructor
@ComponentProfile(summary = "Anonymous access to a SMB share")
public class AnonymousAccess implements AuthenticationProvider {

  @Override
  public AuthenticationContext authContext() {
    return AuthenticationContext.anonymous();
  }
}

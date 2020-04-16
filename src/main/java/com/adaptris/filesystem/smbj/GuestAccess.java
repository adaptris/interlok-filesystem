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

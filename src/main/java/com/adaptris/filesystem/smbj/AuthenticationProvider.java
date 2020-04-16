package com.adaptris.filesystem.smbj;

import com.hierynomus.smbj.auth.AuthenticationContext;

public interface AuthenticationProvider {

  AuthenticationContext authContext() throws Exception;
}

package com.adaptris.filesystem.smbj;

import com.hierynomus.smbj.SmbConfig;

public interface ConfigProvider {

  SmbConfig smbConfig() throws Exception;
}

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

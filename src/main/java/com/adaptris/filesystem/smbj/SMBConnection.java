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

import java.util.concurrent.TimeUnit;
import jakarta.validation.Valid;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.NoOpConnection;
import com.hierynomus.protocol.commons.IOUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.common.SmbPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

/**
 * Wraps common functionality for both producers and consumers.
 * 
 * <p>
 * <ul>
 * <li>Stores the authentication</li>
 * <li>Stores SMB configuration (such as timeout)</li>
 * <li>Keeps an (unconfigurable) cache of connections; 50 entries with an expiry of 15 minutes; to alleviate some of the overhead in
 * create a connection to a remote SMB share.</li>
 * </ul>
 * </p>
 * 
 * @config smb-connection
 *
 */
@XStreamAlias("smb-connection")
@NoArgsConstructor
@ComponentProfile(summary = "Connection to a SMB Share", since = "3.10.1")
public class SMBConnection extends NoOpConnection {

  private static final int MAX_CACHE_ENTRIES = 50;

  /**
   * The authentication to use for the SMB share.
   * <p>
   * If not specified, then the deault is {@link AnonymousAccess}.
   * </p>
   */
  @Getter
  @Setter
  @Valid
  @InputFieldDefault(value = "smb-anonymous-access")
  private AuthenticationProvider authentication;

  /**
   * The configuration to apply for SMB
   * <p>
   * If not specified, then the deault is {@link DefaultConfig}.
   * </p>
   */
  @Getter
  @Setter
  @Valid
  @InputFieldDefault(value = "smb-default-config")
  private ConfigProvider config;

  // a case of premature optimisation, but let's keep a cache based on the SMBServer + sharename
  // of the Workers
  private transient ExpiringMap<SmbPath, Connector> cache;
  private transient Object connectorLock = new Object();

  @Override
  protected void initConnection() throws CoreException {
    cache = ExpiringMap.builder().maxSize(MAX_CACHE_ENTRIES).asyncExpirationListener(new CloseOnExpiry())
        .expirationPolicy(ExpirationPolicy.ACCESSED).expiration(15L, TimeUnit.MINUTES).build();
  }

  @Override
  protected void closeConnection() {
    cache.values().forEach((w) -> IOUtils.closeSilently(w));
    cache.clear();
  }


  public SMBConnection withAuthenticationProvider(AuthenticationProvider a) {
    setAuthentication(a);
    return this;
  }

  public SMBConnection withConfig(ConfigProvider c) {
    setConfig(c);
    return this;
  }


  protected AuthenticationProvider authenticationProvider() {
    return ObjectUtils.defaultIfNull(getAuthentication(), new AnonymousAccess());
  }

  protected ConfigProvider config() {
    return ObjectUtils.defaultIfNull(getConfig(), new DefaultConfig());
  }

  protected SMBClient createClient() throws Exception {
    return new SMBClient(config().smbConfig());
  }

  @Synchronized("connectorLock")
  public Connector createOrGetWorker(SmbPath path) throws Exception {
    Connector w = cache.get(path);
    if (w == null || !w.isConnected()) {
      w = new Connector(path, this).connect(createClient());
      cache.put(path, w);
    }
    return w;
  }

  protected static class CloseOnExpiry implements ExpirationListener<SmbPath, Connector> {
    @Override
    public void expired(SmbPath key, Connector value) {
      IOUtils.closeSilently(value);
    }
  }
}

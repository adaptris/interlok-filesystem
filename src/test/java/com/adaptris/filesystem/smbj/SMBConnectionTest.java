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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.filesystem.smbj.SMBConnection.CloseOnExpiry;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

public class SMBConnectionTest {

  @Test
  public void testLifecycle() throws Exception {
    SMBConnection c = new SMBConnection().withAuthenticationProvider(new AnonymousAccess()).withConfig(new DefaultConfig());
    try {
      LifecycleHelper.initAndStart(c);
    } finally {
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testCreateClient() throws Exception {
    SMBConnection c = new SMBConnection().withAuthenticationProvider(new AnonymousAccess()).withConfig(new DefaultConfig());
    try {
      LifecycleHelper.initAndStart(c);
      assertNotNull(c.createClient());
    } finally {
      LifecycleHelper.stopAndClose(c);
    }
  }

  @Test
  public void testCacheExpiryListener() throws Exception {
    ExpiringMap cache = ExpiringMap.builder().maxSize(10).asyncExpirationListener(new CloseOnExpiry())
        .expirationPolicy(ExpirationPolicy.ACCESSED).expiration(500L, TimeUnit.MILLISECONDS).build();
    SmbPath path = SmbPath.parse("//1.1.1.1/shareName/path/to/dir");
    Connector c = new Connector(path, new SMBConnection());
    cache.put(path, c);
    Awaitility.await()
     .atMost(Duration.ofSeconds(2))
     .with()
     .pollInterval(Duration.ofMillis(100))
     .until(cache::size, equalTo(0));
    assertFalse(c.isConnected());
  }


  @Test
  public void testCreateConnector() throws Exception {
    SmbPath p1 = SmbPath.parse("//1.1.1.1/shareName/path/to/dir");
    SmbPath p2 = SmbPath.parse("//1.1.1.1/shareName/path/to/other/dir");
    MockConnection conn = new MockConnection();
    try {
      LifecycleHelper.initAndStart(conn);
      Connector c1 = conn.createOrGetWorker(p1);
      Connector c2 = conn.createOrGetWorker(p1);
      Connector c3 = conn.createOrGetWorker(p2);
      assertSame(c1, c2);
      assertNotSame(c1, c3);
      // close c2, so it should be invalidated out of the cache.
      c2.close();
      c2 = conn.createOrGetWorker(p1);
      assertNotSame(c1, c2);
      assertFalse(c1.isConnected());
    } finally {
      LifecycleHelper.stopAndClose(conn);
    }
  }

  @Test
  public void testConnector() throws Exception {
    SmbPath p1 = SmbPath.parse("//1.1.1.1/shareName/path/to/dir");
    MockConnection conn = new MockConnection();
    try {
      LifecycleHelper.initAndStart(conn);
      SMBClient client = conn.createClient();
      Connector c1 = new Connector(p1, conn);
      assertFalse(c1.isConnected());
      assertNull(c1.getDiskShare());
      c1.connect(client);
      assertTrue(c1.isConnected());

      c1.close();
      assertFalse(c1.isConnected());

    } finally {
      LifecycleHelper.stopAndClose(conn);
    }
  }

  private class MockConnection extends SMBConnection {
    private transient SMBClient client;
    private transient DiskShare diskShare;
    private transient Connection connection;
    private transient Session session;


    public MockConnection() throws Exception {
      client = Mockito.mock(SMBClient.class);
      diskShare = Mockito.mock(DiskShare.class);
      connection = Mockito.mock(Connection.class);
      session = Mockito.mock(Session.class);

      when(client.connect(anyString())).thenReturn(connection);
      when(client.connect(anyString(), anyInt())).thenReturn(connection);

      when(connection.authenticate(any())).thenReturn(session);
      when(connection.isConnected()).thenReturn(true);
      when(session.connectShare(anyString())).thenReturn(diskShare);
    }

    @Override
    protected SMBClient createClient() throws Exception {
      return client;
    }
  }
}

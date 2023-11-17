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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import com.adaptris.util.TimeInterval;
import com.hierynomus.smbj.SmbConfig;

public class ConfigProviderTest {

  @Test
  public void testDefaultConfig() throws Exception {
    assertNotNull(new DefaultConfig().smbConfig());
  }

  @Test
  public void testExtendedConfig() throws Exception {
    assertNotNull(new ExtendedConfig().smbConfig());
  }

  @Test
  public void testExtendedConfig_All() throws Exception {
    TimeInterval timeout = new TimeInterval(10L, TimeUnit.SECONDS);
    ExtendedConfig cfg = new ExtendedConfig();
    cfg.setBufferSize(1024);
    cfg.setDfsEnabled(true);
    cfg.setMultiProtocolNegotiate(true);
    cfg.setSigningRequired(true);
    cfg.setSoTimeout(timeout);
    cfg.setTimeout(timeout);
    cfg.setWorkstationName("workstationName");
    assertNotNull(cfg.smbConfig());
    SmbConfig smb = cfg.smbConfig();
    assertEquals(timeout.toMilliseconds(), smb.getWriteTimeout());
    assertEquals(timeout.toMilliseconds(), smb.getTransactTimeout());
    assertEquals(timeout.toMilliseconds(), smb.getReadTimeout());
    assertTrue(smb.isUseMultiProtocolNegotiate());
  }
}

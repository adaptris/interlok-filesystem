package com.adaptris.filesystem.smbj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
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

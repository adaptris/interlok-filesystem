package com.adaptris.filesystem.smbj;

import static com.adaptris.filesystem.smbj.SMBProducerTest.STANDARD_PAYLOAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class ReadFileServiceTest extends ServiceCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ReadFileService().withConnection(new SMBConnection()).withPath(SMBProducerTest.SMB_PATH + "\\filename");
  }


  @Test
  public void testService() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    ReadFileService service = new ReadFileService().withConnection(new MockConnection()).withPath(SMBProducerTest.SMB_PATH);
    assertNotEquals(STANDARD_PAYLOAD, msg.getContent());
    execute(service, msg);
    assertEquals(STANDARD_PAYLOAD, msg.getContent());
  }


  @Test(expected = ServiceException.class)
  public void testService_Broken() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage();
    ReadFileService service = new ReadFileService().withConnection(new MockConnection()).withPath(SMBProducerTest.SMB_PATH);
    execute(service, msg);
  }


  private class MockConnection extends SMBConnection {
    private transient SMBClient client;
    private transient DiskShare diskShare;
    private transient Connection connection;
    private transient Session session;
    private transient File smbFile;


    public MockConnection() throws Exception {
      client = Mockito.mock(SMBClient.class);
      diskShare = Mockito.mock(DiskShare.class);
      connection = Mockito.mock(Connection.class);
      session = Mockito.mock(Session.class);
      smbFile = Mockito.mock(File.class);

      when(client.connect(anyString())).thenReturn(connection);
      when(client.connect(anyString(), anyInt())).thenReturn(connection);

      when(connection.authenticate(any())).thenReturn(session);
      when(connection.isConnected()).thenReturn(true);
      when(session.connectShare(anyString())).thenReturn(diskShare);

      when(diskShare.openFile(anyString(), any(), any(), any(), any(), any())).thenReturn(smbFile);
      doThrow(new RuntimeException()).doNothing().when(diskShare).rm(anyString());

      when(smbFile.getInputStream()).thenReturn(IOUtils.toInputStream(STANDARD_PAYLOAD, StandardCharsets.UTF_8));

    }

    @Override
    protected SMBClient createClient() throws Exception {
      return client;
    }

  }


}

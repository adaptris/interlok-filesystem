package com.adaptris.filesystem.smbj;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredDestination;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.stubs.MockEncoder;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class SMBProducerTest extends ProducerCase {

  public static final String SMB_PATH = "\\\\1.1.1.1\\shareName\\path\\to\\dir";
  public static final String STANDARD_PAYLOAD = "Hello World";


  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected StandaloneProducer retrieveObjectForSampleConfig() {
    SMBProducer producer = new SMBProducer().withMode(new CreateMode()).withFilenameCreator(new FormattedFilenameCreator());
    producer.setDestination(new ConfiguredDestination(SMB_PATH));
    return new StandaloneProducer(new SMBConnection(), producer);
  }


  @Test
  public void testProduce() throws Exception {
    SMBProducer producer = new SMBProducer().withMode(new CreateMode()).withFilenameCreator(new FormattedFilenameCreator());
    producer.setDestination(new ConfiguredDestination(SMB_PATH));
    StandaloneProducer sp = new StandaloneProducer(new MockConnection(), producer);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    ServiceCase.execute(sp, msg);
  }

  @Test
  public void testProduce_WithEncoder() throws Exception {
    SMBProducer producer = new SMBProducer();
    producer.setEncoder(new MockEncoder());
    producer.setDestination(new ConfiguredDestination(SMB_PATH));
    StandaloneProducer sp = new StandaloneProducer(new MockConnection(), producer);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    ServiceCase.execute(sp, msg);
  }

  @Test(expected = ServiceException.class)
  public void testBroken() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage(STANDARD_PAYLOAD);

    SMBProducer producer = new SMBProducer();
    producer.setDestination(new ConfiguredDestination(SMB_PATH));
    StandaloneProducer sp = new StandaloneProducer(new MockConnection(), producer);

    ServiceCase.execute(sp, msg);

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

      when(smbFile.getOutputStream()).thenReturn(new ByteArrayOutputStream());
      when(smbFile.getOutputStream(anyBoolean())).thenReturn(new ByteArrayOutputStream());

    }

    @Override
    protected SMBClient createClient() throws Exception {
      return client;
    }
  }

}

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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.FormattedFilenameCreator;
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
import com.adaptris.interlok.junit.scaffolding.ExampleProducerCase;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class SMBProducerTest extends ExampleProducerCase {

  public static final String SMB_PATH = "\\\\1.1.1.1\\shareName\\path\\to\\dir";
  public static final String STANDARD_PAYLOAD = "Hello World";


  @Override
  protected StandaloneProducer retrieveObjectForSampleConfig() {
    SMBProducer producer = new SMBProducer().withMode(new CreateMode())
        .withFilenameCreator(new FormattedFilenameCreator()).withPath(SMB_PATH);
    return new StandaloneProducer(new SMBConnection(), producer);
  }


  @Test
  public void testProduce() throws Exception {
    SMBProducer producer = new SMBProducer().withMode(new CreateMode())
        .withFilenameCreator(new FormattedFilenameCreator()).withPath(SMB_PATH);
    StandaloneProducer sp = new StandaloneProducer(new MockConnection(), producer);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    ExampleServiceCase.execute(sp, msg);
  }

  @Test
  public void testProduce_WithEncoder() throws Exception {
    SMBProducer producer = new SMBProducer().withPath(SMB_PATH);
    producer.setEncoder(new MockEncoder());
    StandaloneProducer sp = new StandaloneProducer(new MockConnection(), producer);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(STANDARD_PAYLOAD);
    ExampleServiceCase.execute(sp, msg);
  }

  @Test
  public void testBroken() throws Exception {
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.BOTH).newMessage(STANDARD_PAYLOAD);

    SMBProducer producer = new SMBProducer().withPath(SMB_PATH);
    StandaloneProducer sp = new StandaloneProducer(new MockConnection(), producer);
    assertThrows(ServiceException.class, ()->{
      ExampleServiceCase.execute(sp, msg);
    }, "Failed service");

    

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

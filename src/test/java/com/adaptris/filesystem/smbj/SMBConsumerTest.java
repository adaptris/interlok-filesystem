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

import static com.adaptris.filesystem.smbj.SMBProducerTest.SMB_PATH;
import static com.adaptris.filesystem.smbj.SMBProducerTest.STANDARD_PAYLOAD;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.PollerImp.Callback;
import com.adaptris.core.RandomIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.protocol.commons.EnumWithValue.EnumUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;

public class SMBConsumerTest extends ConsumerCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Override
  protected StandaloneConsumer retrieveObjectForSampleConfig() {
    SMBConsumer consumer = new SMBConsumer().withFileFilterImp(null);
    consumer.setPath(SMB_PATH);
    return new StandaloneConsumer(new SMBConnection(), consumer);
  }


  @Test
  public void testConsume() throws Exception {
    MockMessageListener listener = new MockMessageListener();
    SMBConsumer consumer = new SMBConsumer().withFileFilterImp(null);
    assertEquals(CoreConstants.FS_CONSUME_DIRECTORY, consumer.consumeLocationKey());
    consumer.setPath(SMB_PATH);
    PollerCallback callback = new PollerCallback();
    consumer.setPoller(new RandomIntervalPoller(new TimeInterval(1L, TimeUnit.SECONDS)).withPollerCallback(callback));
    consumer.setMaxMessagesPerPoll(5);
    StandaloneConsumer sc = new StandaloneConsumer(new MockConnection(), consumer);
    sc.registerAdaptrisMessageListener(listener);
    try {
      LifecycleHelper.prepare(sc);
      LifecycleHelper.initAndStart(sc);
      Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> listener.messageCount() >= 10);
    } finally {
      LifecycleHelper.stopAndClose(sc);
    }
  }


  @Test
  public void testConsume_WithEncoder() throws Exception {
    MockMessageListener listener = new MockMessageListener();
    SMBConsumer consumer = new SMBConsumer().withFileFilterImp(null).withPath(SMB_PATH);
    consumer.setEncoder(new MockEncoder());
    consumer.setPoller(new RandomIntervalPoller(new TimeInterval(1L, TimeUnit.SECONDS)));
    StandaloneConsumer sc = new StandaloneConsumer(new MockConnection(), consumer);
    sc.registerAdaptrisMessageListener(listener);
    try {
      LifecycleHelper.initAndStart(sc);
      Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .pollInterval(Duration.ofMillis(100))
        .until(() -> listener.messageCount() >= 10);
    } finally {
      LifecycleHelper.stopAndClose(sc);
    }
  }

  private class PollerCallback implements Callback {

    private transient int msgCount = 0;
    @Override
    public void pollTriggered(int msgsAttempted) {
      msgCount += msgsAttempted;
    }

    public int count() {
      return msgCount;
    }
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

      List<FileIdBothDirectoryInformation> fileList = buildFiles(10, 1);
      when(diskShare.list(anyString())).thenThrow(new RuntimeException()).thenReturn(fileList);

      when(diskShare.openFile(anyString(), any(), any(), any(), any(), any())).thenReturn(smbFile);
      doThrow(new RuntimeException()).doNothing().when(diskShare).rm(anyString());

      when(smbFile.getInputStream()).thenReturn(IOUtils.toInputStream(STANDARD_PAYLOAD, StandardCharsets.UTF_8));

    }

    @Override
    protected SMBClient createClient() throws Exception {
      return client;
    }

    // Effectively builds you fileCount + dirCount number of files.
    private List<FileIdBothDirectoryInformation> buildFiles(int fileCount, int dirCount) {
      Random random = ThreadLocalRandom.current();
      List<FileIdBothDirectoryInformation> result = new ArrayList<>();
      for (int i = 0; i < fileCount; i++) {
        result.add(buildFile("file" + i, random.nextInt(1024), false));
      }
      for (int i = 0; i < dirCount; i++) {
        result.add(buildFile("dir" + i, random.nextInt(1024), true));
      }
      return result;
    }

    // The bare minimum that we need.
    private FileIdBothDirectoryInformation buildFile(String name, long size,
        boolean isDir) {
      FileIdBothDirectoryInformation f1 = Mockito.mock(FileIdBothDirectoryInformation.class);
      when(f1.getFileName()).thenReturn(name);
      if (isDir) {
        when(f1.getFileAttributes()).thenReturn(EnumUtils.toLong(EnumSet.of(FileAttributes.FILE_ATTRIBUTE_DIRECTORY)));
      } else {
        when(f1.getFileAttributes()).thenReturn(0L);
      }
      when(f1.getLastAccessTime()).thenReturn(FileTime.now());
      when(f1.getLastWriteTime()).thenReturn(FileTime.now());
      when(f1.getEndOfFile()).thenReturn(size);
      return f1;
    }
  }

}

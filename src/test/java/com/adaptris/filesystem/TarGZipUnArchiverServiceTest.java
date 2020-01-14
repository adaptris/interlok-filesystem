/*
 * Copyright Adaptris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adaptris.filesystem;

import static org.eclipse.jetty.util.IO.delete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConfiguredDestination;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.util.LifecycleHelper;

/**
 * @author mwarman
 */
public class TarGZipUnArchiverServiceTest extends ServiceCase {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testDoServiceFile() throws Exception {
    File file = new File(this.getClass().getClassLoader().getResource("archive.tar.gz").getFile());
    byte[] payload = Files.readAllBytes(Paths.get(file.toURI()));
    TarGZipUnArchiverService service =
        new TarGZipUnArchiverService().withExtractionMode(new FileExtractionMode(new ConfiguredDestination("^file.xml$")));
    LifecycleHelper.initAndStart(service);
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    service.doService(message);
    assertEquals("<root>data</root>\n", message.getContent());
    LifecycleHelper.stopAndClose(service);
  }

  @Test
  public void testDoServiceFileNoMatcher() throws Exception {
    File file = new File(this.getClass().getClassLoader().getResource("archive.tar.gz").getFile());
    byte[] payload = Files.readAllBytes(Paths.get(file.toURI()));
    TarGZipUnArchiverService service = new TarGZipUnArchiverService().withExtractionMode(new FileExtractionMode());
    LifecycleHelper.initAndStart(service);
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    service.doService(message);
    assertEquals("<root>data</root>\n", message.getContent());
    LifecycleHelper.stopAndClose(service);
  }

  @Test
  public void testDoServiceFileWithDirectory() throws Exception {
    File file = new File(this.getClass().getClassLoader().getResource("archive.tar.gz").getFile());
    byte[] payload = Files.readAllBytes(Paths.get(file.toURI()));
    TarGZipUnArchiverService service =
        new TarGZipUnArchiverService().withExtractionMode(new FileExtractionMode(new ConfiguredDestination("^dir/file1.xml$")));
    LifecycleHelper.initAndStart(service);
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    service.doService(message);
    assertEquals("<root>data1</root>\n", message.getContent());
    LifecycleHelper.stopAndClose(service);
  }

  @Test
  public void testDoServiceFileNoMatch() throws Exception {
    File file = new File(this.getClass().getClassLoader().getResource("archive.tar.gz").getFile());
    byte[] payload = Files.readAllBytes(Paths.get(file.toURI()));
    TarGZipUnArchiverService service =
        new TarGZipUnArchiverService().withExtractionMode(new FileExtractionMode(new ConfiguredDestination("^nomatch.xml$")));
    LifecycleHelper.initAndStart(service);
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    service.doService(message);
    assertEquals(Arrays.toString(payload), Arrays.toString(message.getPayload()));
    LifecycleHelper.stopAndClose(service);
  }

  @Test
  public void testDoServiceDirectory() throws Exception {
    File file = new File(this.getClass().getClassLoader().getResource("archive.tar.gz").getFile());
    File directory = createTempDirectory();
    byte[] payload = Files.readAllBytes(Paths.get(file.toURI()));
    TarGZipUnArchiverService service = new TarGZipUnArchiverService()
        .withExtractionMode(new DirectoryExtractionMode(new ConfiguredDestination(directory.getAbsolutePath())));
    LifecycleHelper.initAndStart(service);
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    service.doService(message);
    assertEquals(Arrays.toString(payload), Arrays.toString(message.getPayload()));
    assertDirectory(directory);
    LifecycleHelper.stopAndClose(service);
    for (File childFile : directory.listFiles()) {

      if (childFile.isDirectory()) {
        delete(childFile);
      }
    }
    cleanUpTempDirectory(directory);
  }

  @Test
  public void testDoServiceDirectoryTemp() throws Exception {
    File file = new File(this.getClass().getClassLoader().getResource("archive.tar.gz").getFile());
    byte[] payload = Files.readAllBytes(Paths.get(file.toURI()));
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    File directory = new File(System.getProperty("java.io.tmpdir"), message.getUniqueId());
    TarGZipUnArchiverService service = new TarGZipUnArchiverService().withExtractionMode(new DirectoryExtractionMode());
    LifecycleHelper.initAndStart(service);
    service.doService(message);
    assertEquals(Arrays.toString(payload), Arrays.toString(message.getPayload()));
    assertDirectory(directory);
    LifecycleHelper.stopAndClose(service);
    for (File childFile : directory.listFiles()) {

      if (childFile.isDirectory()) {
        delete(childFile);
      }
    }
    cleanUpTempDirectory(directory);
  }

  @Test
  public void testGetExtractionMode() {
    TarGZipUnArchiverService service = new TarGZipUnArchiverService();
    assertNotNull(service.getExtractionMode());
    assertTrue(service.getExtractionMode() instanceof DirectoryExtractionMode);
    service = new TarGZipUnArchiverService(new FileExtractionMode());
    assertNotNull(service.getExtractionMode());
    assertTrue(service.getExtractionMode() instanceof FileExtractionMode);
    service = new TarGZipUnArchiverService().withExtractionMode(new DirectoryExtractionMode());
    assertNotNull(service.getExtractionMode());
    assertTrue(service.getExtractionMode() instanceof DirectoryExtractionMode);
    service = new TarGZipUnArchiverService();
    service.setExtractionMode(new FileExtractionMode());
    assertNotNull(service.getExtractionMode());
    assertTrue(service.getExtractionMode() instanceof FileExtractionMode);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new TarGZipUnArchiverService();
  }

  public File createTempDirectory() throws IOException {
    File tempDir = File.createTempFile(TarGZipUnArchiverServiceTest.class.getSimpleName(), "", null);
    tempDir.delete();
    if (!tempDir.exists()) {
      tempDir.mkdir();
    }
    return tempDir;
  }

  public void cleanUpTempDirectory(File tempDir) {
    for (final File f : tempDir.listFiles()) {
      f.delete();
    }
    tempDir.delete();
  }

  private void assertDirectory(File directory) throws Exception {
    File file1 = new File(directory, "file.xml");
    File file2 = new File(new File(directory, "dir"), "file1.xml");
    byte[] file1Bytes = Files.readAllBytes(Paths.get(file1.toURI()));
    byte[] file2Bytes = Files.readAllBytes(Paths.get(file2.toURI()));
    assertEquals("<root>data</root>\n", new String(file1Bytes));
    assertEquals("<root>data1</root>\n", new String(file2Bytes));
  }
}

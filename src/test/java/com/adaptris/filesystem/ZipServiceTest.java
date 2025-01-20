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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class ZipServiceTest extends ExampleServiceCase {
  
  @Test
  public void testZipDirectoryService() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    final File basedir = createDirectory(5);
    msg.addMetadata("zip-path", basedir.getCanonicalPath());
    final ZipService zip = new ZipService("%message{zip-path}");

    execute(zip, msg);

    final byte[] zippedData = msg.getPayload();

    msg.setPayload(zippedData);
    execute(new UnzipService(), msg);
    final String unzippedPath = msg.getContent();

    File dir = new File(unzippedPath); // the root extracted directory ($TMP/$message-id)
    dir.deleteOnExit();
    assertTrue(dir.isDirectory());
    dir = new File(dir.getAbsolutePath(), basedir.getName());
    dir.deleteOnExit();
    assertTrue(dir.isDirectory());
    for (final File f : dir.listFiles()) {
      final File f2 = new File(dir, f.getName());
      f2.deleteOnExit();
      assertTrue(f2.exists());
    }
  }

  @Test
  public void testZipDirectoryServiceSingleFile() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("zip-path", "build.gradle");
    final ZipService zip = new ZipService();
    zip.setDirectoryPath("%message{zip-path}");
    execute(zip, msg);

    final byte[] zippedData = msg.getPayload();

    msg.setPayload(zippedData);
    execute(new UnzipService(), msg);
    final String unzippedPath = msg.getContent();

    final File dir = new File(unzippedPath);
    dir.deleteOnExit();
    assertTrue(dir.isDirectory());
    final File file = new File(dir, "build.gradle");
    file.deleteOnExit();
    assertTrue(file.exists());
  }

  @Test
  public void testZipDirectoryServiceSingleFileAbsolutePath() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("zip-path", new File("build.gradle").getAbsolutePath());
    final ZipService zip = new ZipService();
    zip.setDirectoryPath("%message{zip-path}");
    execute(zip, msg);

    final byte[] zippedData = msg.getPayload();

    msg.setPayload(zippedData);
    execute(new UnzipService(), msg);
    final String unzippedPath = msg.getContent();

    final File dir = new File(unzippedPath);
    dir.deleteOnExit();
    assertTrue(dir.isDirectory());
    final File file = new File(dir, "build.gradle");
    file.deleteOnExit();
    assertTrue(file.exists());
  }

  @Test
  public void testZipDirectoryServiceFailure() throws Exception {
    final AdaptrisMessage msg = new DefectiveMessageFactory().newMessage();
    msg.addMetadata("zip-path", "ivy");
    try {
      final ZipService zip = new ZipService();
      zip.setDirectoryPath("%message{zip-path}");
      execute(zip, msg);
      fail();
    } catch (@SuppressWarnings("unused") final ServiceException expected) {
      /* expected result */
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new ZipService("/path/to/directory");
  }

  private File createDirectory(final int fileCount) throws IOException {
    final File root = TempFileUtils.createTrackedDir(this);
    root.deleteOnExit();
    for (int i = 0; i < fileCount; i++) {
      final File f = TempFileUtils.createTrackedFile("zippy", "", root, this);
      FileUtils.write(f, "Hello World", Charset.defaultCharset());
      f.deleteOnExit();
    }
    return root;
  }
}

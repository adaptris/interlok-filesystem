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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.IOException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.filesystem.stubs.DefectiveFileBasedAdaptrisMessage;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

/**
 * @author mwarman
 */
public class CommonsCompressUnzipServiceTest extends ExampleServiceCase {

  private File tempDir;
  private String tempDirCanonicalPath;


  @BeforeEach
  public void setUp() throws IOException {
    tempDir = File.createTempFile(CommonsCompressUnzipServiceTest.class.getSimpleName(), "", null);
    tempDir.delete();
    if (!tempDir.exists()) {
      tempDir.mkdir();
    }
    tempDirCanonicalPath = tempDir.getCanonicalPath();
  }

  @AfterEach
  public void tearDown() {
    for (final File f : tempDir.listFiles()) {
      f.delete();
    }
    tempDir.delete();
  }


  @Test
  public void testConstruct() {
    CommonsCompressUnzipService commonsCompressUnzipService = new CommonsCompressUnzipService();
    assertNull(commonsCompressUnzipService.getFilenameMatch());
    commonsCompressUnzipService = new CommonsCompressUnzipService("^file\\.xml$");
    assertNotNull(commonsCompressUnzipService.getFilenameMatch());
    assertEquals("^file\\.xml$", commonsCompressUnzipService.getFilenameMatch());
  }

  @Test
  public void testSetFilenameMatch() {
    CommonsCompressUnzipService commonsCompressUnzipService = new CommonsCompressUnzipService();
    assertNull(commonsCompressUnzipService.getFilenameMatch());
    commonsCompressUnzipService.setFilenameMatch("^file\\.xml$");
    assertNotNull(commonsCompressUnzipService.getFilenameMatch());
    assertEquals("^file\\.xml$", commonsCompressUnzipService.getFilenameMatch());
  }

  @Test
  public void testDoServiceFileBackedSingleFile() throws Exception {
    FileBackedMessageFactory factory = new FileBackedMessageFactory();
    factory.setCreateTempDir(false);
    factory.setTempDirectory(tempDirCanonicalPath);
    AdaptrisMessage msg = factory.newMessage();

    String context = "Hello World!";

    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(msg.getOutputStream())) {
      ZipArchiveEntry ze = new ZipArchiveEntry("file.xml");
      ze.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
    }

    CommonsCompressUnzipService service = new CommonsCompressUnzipService();
    service.doService(msg);
    assertEquals(context, msg.getContent());
  }

  @Test
  public void testDoServiceFileBackedWithDirectory() throws Exception {
    FileBackedMessageFactory factory = new FileBackedMessageFactory();
    factory.setCreateTempDir(false);
    factory.setTempDirectory(tempDirCanonicalPath);
    AdaptrisMessage msg = factory.newMessage();

    String context = "Hello World!";

    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(msg.getOutputStream())) {
      zos.putArchiveEntry(new ZipArchiveEntry("folder/"));
      ZipArchiveEntry ze = new ZipArchiveEntry("folder/file.xml");
      ze.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
    }

    CommonsCompressUnzipService service = new CommonsCompressUnzipService();
    service.doService(msg);
    assertEquals(context, msg.getContent());
  }

  @Test
  public void testDoServiceFileBackedMultipleFile() throws Exception {
    FileBackedMessageFactory factory = new FileBackedMessageFactory();
    factory.setCreateTempDir(false);
    factory.setTempDirectory(tempDirCanonicalPath);
    AdaptrisMessage msg = factory.newMessage();

    String context = "Hello World!";
    String context2 = "Hello FooBar!";

    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(msg.getOutputStream())) {
      ZipArchiveEntry ze1 = new ZipArchiveEntry("file1.xml");
      ze1.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze1);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
      ZipArchiveEntry ze2 = new ZipArchiveEntry("file2.xml");
      ze2.setSize(context2.getBytes().length);
      zos.putArchiveEntry(ze2);
      zos.write(context2.getBytes());
      zos.closeArchiveEntry();
    }

    CommonsCompressUnzipService service = new CommonsCompressUnzipService();
    service.doService(msg);
    assertEquals(context, msg.getContent());
  }

  @Test
  public void testDoServiceFileBackedMultipleFileNameMatcher() throws Exception {
    FileBackedMessageFactory factory = new FileBackedMessageFactory();
    factory.setCreateTempDir(false);
    factory.setTempDirectory(tempDirCanonicalPath);
    AdaptrisMessage msg = factory.newMessage();

    String context = "Hello World!";
    String context2 = "Hello FooBar!";

    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(msg.getOutputStream())) {
      ZipArchiveEntry ze1 = new ZipArchiveEntry("file1.xml");
      ze1.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze1);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
      ZipArchiveEntry ze2 = new ZipArchiveEntry("file2.xml");
      ze2.setSize(context2.getBytes().length);
      zos.putArchiveEntry(ze2);
      zos.write(context2.getBytes());
      zos.closeArchiveEntry();
    }

    CommonsCompressUnzipService service = new CommonsCompressUnzipService("^file2\\.xml$");
    service.doService(msg);
    assertEquals(context2, msg.getContent());
  }

  @Test
  public void testDoServiceFileBackedMultipleInDirectoryFileNameMatcher() throws Exception {
    FileBackedMessageFactory factory = new FileBackedMessageFactory();
    factory.setCreateTempDir(false);
    factory.setTempDirectory(tempDirCanonicalPath);
    AdaptrisMessage msg = factory.newMessage();

    String context = "Hello World!";
    String context2 = "Hello FooBar!";

    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(msg.getOutputStream())) {
      ZipArchiveEntry ze1 = new ZipArchiveEntry("file1.xml");
      ze1.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze1);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
      zos.putArchiveEntry(new ZipArchiveEntry("folder/"));
      ZipArchiveEntry ze2 = new ZipArchiveEntry("folder/file2.xml");
      ze2.setSize(context2.getBytes().length);
      zos.putArchiveEntry(ze2);
      zos.write(context2.getBytes());
      zos.closeArchiveEntry();
    }

    CommonsCompressUnzipService service = new CommonsCompressUnzipService("^.*file2\\.xml$");
    service.doService(msg);
    assertEquals(context2, msg.getContent());
  }

  @Test
  public void testDoServiceFileBackedMultipleFileNameMatcherNoMatch() throws Exception {
    FileBackedMessageFactory factory = new FileBackedMessageFactory();
    factory.setCreateTempDir(false);
    factory.setTempDirectory(tempDirCanonicalPath);
    AdaptrisMessage msg = factory.newMessage();

    String context = "Hello World!";
    String context2 = "Hello FooBar!";

    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(msg.getOutputStream())) {
      ZipArchiveEntry ze1 = new ZipArchiveEntry("file1.xml");
      ze1.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze1);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
      ZipArchiveEntry ze2 = new ZipArchiveEntry("file2.xml");
      ze2.setSize(context2.getBytes().length);
      zos.putArchiveEntry(ze2);
      zos.write(context2.getBytes());
      zos.closeArchiveEntry();
    }
    String originalMessage = msg.getContent();
    CommonsCompressUnzipService service = new CommonsCompressUnzipService("^file3\\.xml$");
    service.doService(msg);
    assertEquals(originalMessage, msg.getContent());
  }

  @Test
  public void testDoServiceNoneFileBackMessage() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    String context = "Hello World!";

    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(msg.getOutputStream())) {
      ZipArchiveEntry ze1 = new ZipArchiveEntry("file1.xml");
      ze1.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze1);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
    }
    String originalMessage = msg.getContent();
    CommonsCompressUnzipService service = new CommonsCompressUnzipService();
    service.doService(msg);
    assertEquals(originalMessage, msg.getContent());
  }

  @Test
  public void testDoServiceFailure() throws Exception {
    DefectiveMessageFactory defectiveMessageFactory = new DefectiveMessageFactory(DefectiveMessageFactory.WhenToBreak.OUTPUT);
    String context = "Hello World!";
    File zip = new File(new File(tempDirCanonicalPath), "test.zip");
    try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zip)) {
      ZipArchiveEntry ze1 = new ZipArchiveEntry("file1.xml");
      ze1.setSize(context.getBytes().length);
      zos.putArchiveEntry(ze1);
      zos.write(context.getBytes());
      zos.closeArchiveEntry();
    }
    AdaptrisMessage msg = new DefectiveFileBasedAdaptrisMessage(defectiveMessageFactory, zip);
    CommonsCompressUnzipService service = new CommonsCompressUnzipService();
    try {
      service.doService(msg);
      fail("Expect ServiceException not thrown");
    } catch (ServiceException ex) {
      // expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new CommonsCompressUnzipService("^file\\.xml$");
  }
}

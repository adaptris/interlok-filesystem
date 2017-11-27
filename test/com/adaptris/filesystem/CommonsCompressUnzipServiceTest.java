package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.filesystem.stubs.DefectiveFileBasedAdaptrisMessage;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author mwarman
 */
public class CommonsCompressUnzipServiceTest extends ServiceCase {

  private File tempDir;
  private String tempDirCanonicalPath;

  @Before
  public void setUp() throws IOException {
    tempDir = File.createTempFile(CommonsCompressUnzipServiceTest.class.getSimpleName(), "", null);
    tempDir.delete();
    if (!tempDir.exists())
    {
      tempDir.mkdir();
    }
    tempDirCanonicalPath = tempDir.getCanonicalPath();
  }

  @After
  public void tearDown()
  {
    for (final File f : tempDir.listFiles())
    {
      f.delete();
    }
    tempDir.delete();
  }


  @Test
  public void testConstruct(){
    CommonsCompressUnzipService commonsCompressUnzipService = new CommonsCompressUnzipService();
    assertNull(commonsCompressUnzipService.getFilenameMatch());
    commonsCompressUnzipService = new CommonsCompressUnzipService("^file\\.xml$");
    assertNotNull(commonsCompressUnzipService.getFilenameMatch());
    assertEquals("^file\\.xml$", commonsCompressUnzipService.getFilenameMatch());
  }

  @Test
  public void testSetFilenameMatch(){
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
    } catch (ServiceException ex){
      //expected
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new CommonsCompressUnzipService("^file\\.xml$");
  }
}
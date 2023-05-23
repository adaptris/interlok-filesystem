package com.adaptris.filesystem;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

/**
 * @author mwarman
 */
public class DeleteFileServiceTest extends ExampleServiceCase {

  private File directoryPath;

  @BeforeEach
  public void setUp() throws Exception {
    directoryPath = createTempDirectory();
  }

  @AfterEach
  public void tearDown() throws Exception {
    cleanUpTempDirectory(directoryPath);
  }

  @Test
  public void testServiceDeleteEmptyParent() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    DeleteFileService service = new DeleteFileService();
    service.setDeleteEmptyParent(true);
    File file1 = new File(directoryPath, "text1.xml");
    assertTrue(file1.exists());
    service.setPath(new ConstantDataInputParameter(file1.getAbsolutePath()));
    service.doService(message);
    assertFalse(file1.exists());
    File file2Parent = new File(directoryPath, "recursive");
    File file2 = new File(file2Parent, "text3.xml");
    assertTrue(file2Parent.exists());
    assertTrue(file2.exists());
    service.setPath(new ConstantDataInputParameter(file2.getAbsolutePath()));
    service.doService(message);
    assertFalse(file2Parent.exists());
    assertFalse(file2.exists());
  }

  @Test
  public void testServiceWithoutDeleteEmptyParent() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    DeleteFileService service = new DeleteFileService();
    service.setDeleteEmptyParent(false);
    File file1 = new File(directoryPath, "text1.xml");
    assertTrue(file1.exists());
    service.setPath(new ConstantDataInputParameter(file1.getAbsolutePath()));
    service.doService(message);
    assertFalse(file1.exists());
    File file2Parent = new File(directoryPath, "recursive");
    File file2 = new File(file2Parent, "text3.xml");
    assertTrue(file2Parent.exists());
    assertTrue(file2.exists());
    service.setPath(new ConstantDataInputParameter(file2.getAbsolutePath()));
    service.doService(message);
    assertTrue(file2Parent.exists());
    assertFalse(file2.exists());
  }

  @Override
  protected DeleteFileService retrieveObjectForSampleConfig() {
    return new DeleteFileService().withPath(new ConstantDataInputParameter("/home/interlok/file.xml"));
  }

  public File createTempDirectory() throws IOException {
    File tempDir = File.createTempFile(DirectoryListingProvider.class.getSimpleName(), "", null);
    tempDir.delete();
    if (!tempDir.exists()) {
      tempDir.mkdir();
    }
    new FileOutputStream(new File(tempDir, "text1.xml")).close();
    new FileOutputStream(new File(tempDir, "text2.xml")).close();
    File recursive = new File(tempDir, "recursive");
    recursive.mkdir();
    new FileOutputStream(new File(recursive, "text3.xml")).close();
    return tempDir;
  }

  public void cleanUpTempDirectory(File tempDir) {
    for (final File f : FileUtils.listFiles(tempDir, null, true)) {
      f.delete();
    }
    File recursive = new File(tempDir, "recursive");
    recursive.delete();
    tempDir.delete();
  }
}

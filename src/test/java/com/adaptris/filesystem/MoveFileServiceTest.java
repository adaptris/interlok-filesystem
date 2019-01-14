package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.eclipse.jetty.util.IO.delete;

/**
 * @author mwarman
 */
public class MoveFileServiceTest extends ServiceCase {

  @Test
  public void testCannotMoveFile() throws Exception{
    File directory = createTempDirectory();
    File file = new File(directory, "file1.txt");
    File output = new File(directory, "file2.txt");
    FileUtils.writeStringToFile(file, "Hello World");
    MoveFileService service = new MoveFileService()
        .withNewPath(output.getAbsolutePath())
        .withOriginalPath(file.getAbsolutePath())
        .withMoveDirectory(true);
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertFalse(file.exists());
    assertTrue(output.exists());
    assertEquals("Hello World", new String(Files.readAllBytes(Paths.get(output.toURI()))));
    cleanUpTempDirectory(directory);
  }

  @Test
  public void testMoveFileWithMoveDirectory() throws Exception{
    File directory = createTempDirectory();
    File file = new File(directory, "file1.txt");
    File output = new File(directory, "file2.txt");
    FileUtils.writeStringToFile(file, "Hello World");
    MoveFileService service = new MoveFileService()
        .withNewPath(output.getAbsolutePath())
        .withOriginalPath(file.getAbsolutePath())
        .withMoveDirectory(true);
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertFalse(file.exists());
    assertTrue(output.exists());
    assertEquals("Hello World", new String(Files.readAllBytes(Paths.get(output.toURI()))));
    cleanUpTempDirectory(directory);
  }

  @Test
  public void testMoveDirectoryWithMoveDirectory() throws Exception{
    File directory = createTempDirectory();
    File file = new File(new File(directory, "dir1"), "file1.txt");
    File output = new File(new File(directory, "dir2"), "file1.txt");
    FileUtils.writeStringToFile(file, "Hello World");
    MoveFileService service = new MoveFileService()
        .withNewPath(output.getParentFile().getAbsolutePath())
        .withOriginalPath(file.getParentFile().getAbsolutePath())
        .withMoveDirectory(true);
    //What does this empty method do and why is it needed
    service.prepare();
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertFalse(file.getParentFile().exists());
    assertFalse(file.exists());
    assertTrue(output.getParentFile().exists());
    assertTrue(output.exists());
    assertEquals("Hello World", new String(Files.readAllBytes(Paths.get(output.toURI()))));
    //What does this empty method do and why is it needed
    service.closeService();
    cleanUpTempPopulatedDir(directory);
  }

  @Test
  public void testMoveDirectoryWithoutMoveDirectory() throws Exception{
    File directory = createTempDirectory();
    File file = new File(new File(directory, "dir1"), "file1.txt");
    File output = new File(new File(directory, "dir2"), "file1.txt");
    FileUtils.writeStringToFile(file, "Hello World");
    MoveFileService service = new MoveFileService()
        .withNewPath(output.getParentFile().getAbsolutePath())
        .withOriginalPath(file.getParentFile().getAbsolutePath())
        .withMoveDirectory(false);
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertTrue(file.getParentFile().exists());
    assertTrue(file.exists());
    assertFalse(output.getParentFile().exists());
    assertFalse(output.exists());
    assertEquals("Hello World", new String(Files.readAllBytes(Paths.get(file.toURI()))));
    cleanUpTempPopulatedDir(directory);
  }

  public File createTempDirectory() throws IOException {
    File tempDir = File.createTempFile(DeleteDirectoryServiceTest.class.getSimpleName(), "", null);
    tempDir.delete();
    if (!tempDir.exists())
    {
      tempDir.mkdir();
    }
    return tempDir;
  }

  public void cleanUpTempDirectory(File tempDir)
  {
    for (final File f : tempDir.listFiles())
    {
      f.delete();
    }
    tempDir.delete();
  }

  public void cleanUpTempPopulatedDir(File tempDir)
  {
    try {
      for (File childFile : tempDir.listFiles()) {

        if (childFile.isDirectory()) {
          delete(childFile);
        }
      }
      Files.delete(tempDir.toPath());
    } catch (IOException e) {
      //deleting file failed
      e.printStackTrace();
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new MoveFileService()
        .withOriginalPath("./from/text.xml")
        .withNewPath("./to/text.xml")
        .withMoveDirectory(false);
  }
}

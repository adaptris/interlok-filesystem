package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.io.FileUtils.write;
import static org.eclipse.jetty.util.IO.delete;
import static org.junit.Assert.*;

  public class MoveDirectoryServiceTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testMoveDirectoryWithMoveDirectory() throws Exception {
      File directory = createTempDirectory();
      File file = new File(new File(directory, "dir1"), "file1.txt");
      File output = new File(new File(directory, "dir2"), "file1.txt");
      FileUtils.writeStringToFile(file, "Hello World");
      MoveDirectoryService service = new MoveDirectoryService();
      //Empty methods, used to increase coverage
      service.prepare();
      service.init();

      service.setNewDirectoryPath(output.getParentFile().getAbsolutePath());
      service.setOriginalDirectoryPath(file.getParentFile().getAbsolutePath());
      service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());

      assertFalse("Should not have \'getParentFile\'", file.getParentFile().exists());
      assertTrue("Should have dir1 in \'getPath\'", file.getPath().contains("dir1"));
      assertTrue("Should have \'getParentFile\'", output.getParentFile().exists());
      assertTrue("Should exist", output.exists());
      assertEquals("Should contain Hello World in output file", "Hello World", new String(Files.readAllBytes(Paths.get(output.toURI()))));

      //Empty method used to increase coverage
      service.close();

      cleanUpTempDirectory(directory);
    }

    @Test
    public void testMoveFileInDir() throws Exception {
      File directory = createTempDirectory();
      final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      File file = new File(new File(directory, "tempTestSource"), "file1.txt");
      Files.createDirectories(Paths.get(new File(directory, "tempTestTarget").getAbsolutePath()));
      write(file, "Hello World");
      MoveDirectoryService service = new MoveDirectoryService();

      String target = directory + File.separator + "tempTestTarget";
      String source = directory + File.separator + "tempTestSource";
      File sourceDir = new File (source);
      File targetDir = new File (target);

      service.setNewDirectoryPath(new File(target, "file1.txt").getAbsolutePath());
      service.setOriginalDirectoryPath(file.getAbsolutePath());
      String originalDirPath = service.getOriginalDirectoryPath();
      String newDirPath = service.getNewDirectoryPath();

      service.doService(message);

      assertEquals("Should both have same parent dir", Paths.get(target).getParent(), Paths.get(source).getParent());
      assertEquals("Directory should be parent dir", directory.toPath(), Paths.get(target).getParent());
      assertEquals("Source should contain file1.txt","file1.txt", Paths.get(originalDirPath).getFileName().toString());
      assertEquals("Should not contain file1.txt","ERROR: File not found", searchDirectory(targetDir).toString());
      assertEquals("Should contain Hello World in output file", "Hello World", new String(Files.readAllBytes(Paths.get(originalDirPath))));

      Files.move(Paths.get(originalDirPath).toAbsolutePath(), Paths.get(newDirPath).toAbsolutePath());

      assertEquals("Should not contain file1.txt","ERROR: File not found", searchDirectory(sourceDir).toString());
      assertEquals("Source should contain file1.txt","file1.txt", Paths.get(newDirPath).getFileName().toString());
      assertEquals("Should contain Hello World in output file", "Hello World", new String(Files.readAllBytes(Paths.get(newDirPath))));

      service.doService(message);


      cleanUpTempDirectory(directory);
    }

    public File searchDirectory(File searchDir) {
      String[] list = searchDir.list(new FilenameFilter() {
        @Override
        public boolean accept(File searchDir, String name) {
            return name.matches("file1.txt");
          }
      });
      if (list.length == 0) {
        File notFound = new File("ERROR: File not found");
        return notFound;
      }
      File match = new File (list[0]);
      return match;
    }

    public File createTempDirectory() throws IOException {
      File tempDir = File.createTempFile(DeleteDirectoryServiceTest.class.getSimpleName(), "", null);
      tempDir.delete();
      if (!tempDir.exists()) {
        tempDir.mkdir();
      }
      return tempDir;
    }

    public void cleanUpTempDirectory(File tempDir) {
      try {
        for (File childFile : tempDir.listFiles()) {
          if (childFile.isDirectory()) {
            delete(childFile);
          }
        }
        Files.delete(tempDir.toPath());
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

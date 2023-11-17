package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import static org.eclipse.jetty.util.IO.delete;
import static org.junit.jupiter.api.Assertions.*;

public class DirectoryEntityServiceTest {

    @Test
    public void testDefaults() {
      final DirectoryEntityService directoryEntityService = new DirectoryEntityService();
      assertNull(directoryEntityService.getDebugMode(), "Should instantiate as null");
      assertNull(directoryEntityService.getMetadataKey(), "Should instantiate as null");
      assertNull(directoryEntityService.getDirectoryPath(), "Should instantiate as null");
      assertFalse(directoryEntityService.debugMode(), "Should instantiate as false");
    }

    @Test
    public void testSetDetails() {
      final DirectoryEntityService directoryEntityService = new DirectoryEntityService();
      assertEquals(null, directoryEntityService.getDebugMode(), "Should instantiate as null");

      directoryEntityService.setDebugMode(true);
      directoryEntityService.setDirectoryPath("Foo");
      directoryEntityService.setMetadataKey("Hello World");

      assertEquals(true, directoryEntityService.getDebugMode(), "Should have updated \'getDebugMode\' to true");
      assertEquals("Foo", directoryEntityService.getDirectoryPath(), "Should have updated \'getDirectoryPath\' to Foo");
      assertEquals("Hello World", directoryEntityService.getMetadataKey(), "Should have updated \'getMetadataKey\' to Hello World");
      assertTrue(directoryEntityService.debugMode(), "Should be set to true");
    }

    @Test
    public void testServiceAllNull() throws Exception {
      final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      final DirectoryEntityService directoryEntityService = new DirectoryEntityService();
      directoryEntityService.setDirectoryPath(null);
      directoryEntityService.setMetadataKey(null);

      try {
        directoryEntityService.initService();
        assertThrows(ServiceException.class, ()->{
          directoryEntityService.doService(message);
        }, "File path null");
      }

      catch (ServiceException e) {
        final String msg = "Missing Required Parameters";
        assertEquals("Should match error messages", msg, e.getMessage());
      }

      directoryEntityService.prepare();
      directoryEntityService.closeService();
    }

    @Test
    public void testServiceAllDetails() throws Exception {
      AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      DirectoryEntityService directoryEntityService = new DirectoryEntityService();

      try {
        directoryEntityService.setDirectoryPath("Foo");
        directoryEntityService.setMetadataKey("Foo");
        directoryEntityService.initService();
        directoryEntityService.doService(message);
        assertThrows(FileNotFoundException.class, ()->{
          directoryEntityService.doService(message);
        }, "File not found : " + directoryEntityService.getDirectoryPath());
      }

      catch (ServiceException e) {
        final String msg = "java.io.FileNotFoundException: File not found : " + directoryEntityService.getDirectoryPath();
        assertEquals(msg, e.getMessage(), "Should match error messages");
        }

      directoryEntityService.prepare();
      directoryEntityService.closeService();
    }

    @Test
    public void testServiceDirPath() throws Exception {
      AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      File directory = createTempDirectory();
      DirectoryEntityService directoryEntityService = new DirectoryEntityService();

      try {
        directoryEntityService.setDirectoryPath(directory.toString());
        directoryEntityService.setMetadataKey("Foo");
        message.resolve(directoryEntityService.getDirectoryPath());
        directoryEntityService.initService();
        directoryEntityService.doService(message);
      }

      catch (ServiceException e) {
        final String msg = "java.io.FileNotFoundException: File not found : " + directoryEntityService.getDirectoryPath();
        assertEquals("Should match error messages", msg, e.getMessage());
      }
        //prepare is never declared in service imp as abstract empty in both
        //init + close Service both declared in serviceImp and here
        //setOutput

      directoryEntityService.prepare();
      directoryEntityService.closeService();
      cleanUpTempPopulatedDir(directory);
    }

    @Test
    public void testMetaNullService() throws Exception {
      AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      File directory = createTempDirectory();
      DirectoryEntityService directoryEntityService = new DirectoryEntityService();

      try {
        directoryEntityService.setDirectoryPath(directory.toString());
        directoryEntityService.setMetadataKey(null);
        message.resolve(directoryEntityService.getDirectoryPath());
        directoryEntityService.initService();
        directoryEntityService.doService(message);
      }

      catch (ServiceException e) {
        final String msg = "java.io.FileNotFoundException: File not found : " + directoryEntityService.getDirectoryPath();
        assertEquals("Should match error messages", msg, e.getMessage());
      }

      directoryEntityService.prepare();
      directoryEntityService.closeService();
      cleanUpTempPopulatedDir(directory);
    }

    public File createTempDirectory() throws IOException {
      File tempDir = File.createTempFile(DeleteDirectoryServiceTest.class.getSimpleName(), "", null);
      tempDir.delete();

      if (!tempDir.exists()) {
        tempDir.mkdir();
      }
      return tempDir;
    }

    public void cleanUpTempPopulatedDir(File tempDir) {

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

/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author mwarman
 */
public class DeleteDirectoryServiceTest extends ServiceCase {


  @Test
  public void testDoServiceDirectoryWithFile() throws Exception {
    File directory = createTempDirectory();
    FileUtils.writeStringToFile(new File(directory, "file.txt"), "Hello World");
    DeleteDirectoryService service = new DeleteDirectoryService()
        .withDirectoryPath(directory.getAbsolutePath());
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertFalse(directory.exists());
  }

  @Test
  public void testDoServiceDirectory() throws Exception {
    File directory = createTempDirectory();
    DeleteDirectoryService service = new DeleteDirectoryService()
        .withDirectoryPath(directory.getAbsolutePath());
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertFalse(directory.exists());
  }

  @Test
  public void testDoServiceDirectoryDoesExists() throws Exception {
    File directory = createTempDirectory();
    File noneExisting = new File(directory, "dir");
    DeleteDirectoryService service = new DeleteDirectoryService()
        .withDirectoryPath(noneExisting.getAbsolutePath());
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertTrue(directory.exists());
    cleanUpTempDirectory(directory);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new DeleteDirectoryService().withDirectoryPath("C:\\temp\\directory");
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

  private void assertDirectory(File directory) throws Exception{
    File file1 = new File(directory, "file.xml");
    File file2 = new File(new File(directory, "dir"), "file1.xml");
    byte[] file1Bytes = Files.readAllBytes(Paths.get(file1.toURI()));
    byte[] file2Bytes = Files.readAllBytes(Paths.get(file2.toURI()));
    assertEquals("<root>data</root>\n" , new String(file1Bytes));
    assertEquals("<root>data1</root>\n" , new String(file2Bytes));
  }
}

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

/**
 * @author mwarman
 */
public class DeleteDirectoryServiceTest extends ExampleServiceCase {

  @Test
  public void testDoServiceDirectoryWithFile() throws Exception {
    File directory = createTempDirectory();
    FileUtils.writeStringToFile(new File(directory, "file.txt"), "Hello World", Charset.defaultCharset());
    DeleteDirectoryService service =
        new DeleteDirectoryService().withPath(new ConstantDataInputParameter(directory.getAbsolutePath()));
    LifecycleHelper.initAndStart(service);
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    LifecycleHelper.stopAndClose(service);
    assertFalse(directory.exists());
  }

  @Test
  public void testDoServiceDirectory() throws Exception {
    File directory = createTempDirectory();
    DeleteDirectoryService service =
        new DeleteDirectoryService().withPath(new ConstantDataInputParameter(directory.getAbsolutePath()));
    LifecycleHelper.initAndStart(service);
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    LifecycleHelper.stopAndClose(service);
    assertFalse(directory.exists());
  }

  @Test
  public void testDoServiceDirectoryDoesExists() throws Exception {
    File directory = createTempDirectory();
    File noneExisting = new File(directory, "dir");
    DeleteDirectoryService service =
        new DeleteDirectoryService().withPath(new ConstantDataInputParameter(noneExisting.getAbsolutePath()));
    LifecycleHelper.initAndStart(service);
    service.doService(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    LifecycleHelper.stopAndClose(service);
    assertTrue(directory.exists());
    cleanUpTempDirectory(directory);
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new DeleteDirectoryService().withPath(new ConstantDataInputParameter("C:\\temp\\directory"));
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
    for (final File f : tempDir.listFiles()) {
      f.delete();
    }
    tempDir.delete();
  }

}

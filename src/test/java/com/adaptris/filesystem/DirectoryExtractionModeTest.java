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
import com.adaptris.core.ConfiguredDestination;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author mwarman
 */
public class DirectoryExtractionModeTest {

  @Test
  public void getOutputDirectory() throws Exception{
    DirectoryExtractionMode extractionMode = new DirectoryExtractionMode();
    assertNull(extractionMode.getOutputDirectory());
    extractionMode = new DirectoryExtractionMode(new ConfiguredDestination("value"));
    assertNotNull(extractionMode.getOutputDirectory());
    assertTrue(extractionMode.getOutputDirectory() instanceof ConfiguredDestination);
    assertNotNull(extractionMode.getOutputDirectory());
    assertTrue(extractionMode.getOutputDirectory() instanceof ConfiguredDestination);
    assertEquals("value", extractionMode.getOutputDirectory().getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
    extractionMode = new DirectoryExtractionMode().withOutputDirectory(new ConfiguredDestination("value2"));
    assertNotNull(extractionMode.getOutputDirectory());
    assertTrue(extractionMode.getOutputDirectory() instanceof ConfiguredDestination);
    assertEquals("value2", extractionMode.getOutputDirectory().getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
    extractionMode = new DirectoryExtractionMode();
    extractionMode.setOutputDirectory(new ConfiguredDestination("value3"));
    assertNotNull(extractionMode.getOutputDirectory());
    assertTrue(extractionMode.getOutputDirectory() instanceof ConfiguredDestination);
    assertEquals("value3", extractionMode.getOutputDirectory().getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }
}

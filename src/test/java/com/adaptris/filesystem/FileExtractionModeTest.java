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
public class FileExtractionModeTest {

  @Test
  public void getFilenameMatch() throws Exception{
    FileExtractionMode extractionMode = new FileExtractionMode();
    assertNull(extractionMode.getFilenameMatch());
    extractionMode = new FileExtractionMode(new ConfiguredDestination("value"));
    assertNotNull(extractionMode.getFilenameMatch());
    assertTrue(extractionMode.getFilenameMatch() instanceof ConfiguredDestination);
    assertNotNull(extractionMode.getFilenameMatch());
    assertTrue(extractionMode.getFilenameMatch() instanceof ConfiguredDestination);
    assertEquals("value", extractionMode.getFilenameMatch().getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
    extractionMode = new FileExtractionMode().withFilenameMatch(new ConfiguredDestination("value2"));
    assertNotNull(extractionMode.getFilenameMatch());
    assertTrue(extractionMode.getFilenameMatch() instanceof ConfiguredDestination);
    assertEquals("value2", extractionMode.getFilenameMatch().getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
    extractionMode = new FileExtractionMode();
    extractionMode.setFilenameMatch(new ConfiguredDestination("value3"));
    assertNotNull(extractionMode.getFilenameMatch());
    assertTrue(extractionMode.getFilenameMatch() instanceof ConfiguredDestination);
    assertEquals("value3", extractionMode.getFilenameMatch().getDestination(AdaptrisMessageFactory.getDefaultInstance().newMessage()));
  }
}

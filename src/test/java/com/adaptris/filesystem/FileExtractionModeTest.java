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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author mwarman
 */
public class FileExtractionModeTest {

  @Test
  public void getFilenameMatch() throws Exception{
    FileExtractionMode extractionMode = new FileExtractionMode();
    assertNull(extractionMode.getFilenameMatch());
    extractionMode = new FileExtractionMode("value");
    assertNotNull(extractionMode.getFilenameMatch());
    assertEquals("value", extractionMode.getFilenameMatch());
    extractionMode = new FileExtractionMode().withFilenameMatch("value2");
    assertNotNull(extractionMode.getFilenameMatch());
    assertEquals("value2", extractionMode.getFilenameMatch());
    extractionMode = new FileExtractionMode();
    extractionMode.setFilenameMatch("value3");
    assertNotNull(extractionMode.getFilenameMatch());
    assertEquals("value3", extractionMode.getFilenameMatch());
  }
}

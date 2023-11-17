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
public class DirectoryExtractionModeTest {

  @Test
  public void getOutputDirectory() throws Exception{
    DirectoryExtractionMode extractionMode = new DirectoryExtractionMode();
    assertNull(extractionMode.getOutputDirectory());
    extractionMode = new DirectoryExtractionMode("value");
    assertNotNull(extractionMode.getOutputDirectory());
    assertEquals("value", extractionMode.getOutputDirectory());
    extractionMode = new DirectoryExtractionMode().withOutputDirectory("value2");
    assertNotNull(extractionMode.getOutputDirectory());
    assertEquals("value2", extractionMode.getOutputDirectory());
    extractionMode = new DirectoryExtractionMode();
    extractionMode.setOutputDirectory("value3");
    assertNotNull(extractionMode.getOutputDirectory());
    assertEquals("value3", extractionMode.getOutputDirectory());
  }
}

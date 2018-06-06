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
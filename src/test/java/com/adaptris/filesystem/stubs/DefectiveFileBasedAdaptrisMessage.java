package com.adaptris.filesystem.stubs;

import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.stubs.DefectiveAdaptrisMessage;
import com.adaptris.util.GuidGenerator;

import java.io.File;
import java.io.IOException;

/**
 * @author mwarman
 */
public class DefectiveFileBasedAdaptrisMessage extends DefectiveAdaptrisMessage implements FileBackedMessage {

  private File file;

  public DefectiveFileBasedAdaptrisMessage(AdaptrisMessageFactory amf, File file) throws RuntimeException {
    super(new GuidGenerator(), amf);
    this.file = file;
  }

  @Override
  public void initialiseFrom(File file) throws IOException, RuntimeException {

  }

  @Override
  public File currentSource() {
    return file;
  }

}

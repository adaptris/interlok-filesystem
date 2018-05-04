package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.util.ExceptionHelper;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Pattern;

/**
 * @author mwarman
 */
public class FileExtractionMode implements ExtractionMode {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private final static int BUFFER = 2048;

  private MessageDrivenDestination filenameMatch;

  public FileExtractionMode(){

  }

  public FileExtractionMode(MessageDrivenDestination filenameMatch){
    setFilenameMatch(filenameMatch);
  }

  @Override
  public void extract(TarArchiveInputStream tarArchiveInputStream, AdaptrisMessage adaptrisMessage) throws CoreException {
    try {
      TarArchiveEntry entry;
      while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        String entryName = entry.getName();
        if (getFilenameMatch() != null){
          Pattern pattern = Pattern.compile(getFilenameMatch().getDestination(adaptrisMessage));
          if (!pattern.matcher(entryName).matches()){
            log.trace("The entry [{}] does not match filenameMatch [{}] skipping", entryName, getFilenameMatch());
            continue;
          }
        }
        log.debug("Setting payload using entry [{}] from zip.", entryName);
        try (OutputStream os = adaptrisMessage.getOutputStream()) {
          IOUtils.copy(tarArchiveInputStream, os);
        }
        break;
      }
    } catch (IOException e) {
      ExceptionHelper.rethrowCoreException(e.getMessage(), e);
    }
  }

  public void setFilenameMatch(MessageDrivenDestination filenameMatch) {
    this.filenameMatch = filenameMatch;
  }

  public MessageDrivenDestination getFilenameMatch() {
    return filenameMatch;
  }

  public FileExtractionMode withFilenameMatch(MessageDrivenDestination filenameMatch){
    setFilenameMatch(filenameMatch);
    return this;
  }
}

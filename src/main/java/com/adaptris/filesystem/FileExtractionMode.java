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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * @author mwarman
 * @config file-extraction-mode
 */
@XStreamAlias("file-extraction-mode")
public class FileExtractionMode implements ExtractionMode {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

//  private MessageDrivenDestination filenameMatch;

  @Getter
  @Setter
  @NotBlank
  private String filenameMatch;

  public FileExtractionMode(){

  }

  public FileExtractionMode(String filenameMatch){
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
          Pattern pattern = Pattern.compile(adaptrisMessage.resolveObject(getFilenameMatch()).toString());
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

  public FileExtractionMode withFilenameMatch(String filenameMatch){
    setFilenameMatch(filenameMatch);
    return this;
  }
}

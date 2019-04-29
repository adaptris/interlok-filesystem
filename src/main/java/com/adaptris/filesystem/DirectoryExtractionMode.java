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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageDrivenDestination;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author mwarman
 */
@XStreamAlias("directory-extraction-mode")
public class DirectoryExtractionMode implements ExtractionMode {

  private MessageDrivenDestination outputDirectory;

  public DirectoryExtractionMode(){

  }

  public DirectoryExtractionMode(MessageDrivenDestination outputDirectory){
    setOutputDirectory(outputDirectory);
  }

  @Override
  public void extract(TarArchiveInputStream tarArchiveInputStream, AdaptrisMessage adaptrisMessage) throws CoreException {
    try {
      final File messageTempDirectory;
      if (getOutputDirectory() == null) {
        messageTempDirectory = new File(System.getProperty("java.io.tmpdir"), adaptrisMessage.getUniqueId());
      } else {
        messageTempDirectory = new File(getOutputDirectory().getDestination(adaptrisMessage));
      }
      messageTempDirectory.mkdirs();
      TarArchiveEntry entry;
      while ((entry = (TarArchiveEntry) tarArchiveInputStream.getNextEntry()) != null) {
        File target = ZipFolder.validateTree(messageTempDirectory,
            new File(messageTempDirectory, entry.getName()));
        if (entry.isDirectory()) {
          target.mkdirs();
        } else {
          try (FileOutputStream os = new FileOutputStream(target)) {
            IOUtils.copy(tarArchiveInputStream, os);
          }
        }
      }
    } catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e.getMessage(), e);
    }
  }

  public void setOutputDirectory(MessageDrivenDestination outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public MessageDrivenDestination getOutputDirectory() {
    return outputDirectory;
  }

  public DirectoryExtractionMode withOutputDirectory(MessageDrivenDestination outputDirectory){
    setOutputDirectory(outputDirectory);
    return this;
  }
}

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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * <p>Unzip message payload using commons-compress.</p>
 *
 * <p>NOTE: To use this service the message must be a FileBackedMessage, if this is not the case the service will do
 * nothing.</p>
 *
 * <p>If no filenameMatcher is provided then the service will set the payload of the message to be the first entry found
 * in the zip file. When filenameMatcher is set the service will traverse the zip checking each entry against the
 * matcher. If it does not match anything within the zip, the payload will be left untouched (zipped).</p>
 *
 * @config commons-compress-service
 * @author mwarman
 */
@AdapterComponent
@ComponentProfile(summary = "Unzip the contents of the message", tag = "service,zip,unzip")
@XStreamAlias("commons-compress-unzip-service")
public class CommonsCompressUnzipService extends ServiceImp
{

  @AdvancedConfig
  private String filenameMatch;

  public CommonsCompressUnzipService(){
  }

  public CommonsCompressUnzipService(String filenameMatch){
    this.filenameMatch = filenameMatch;
  }

  @Override
  public void doService(final AdaptrisMessage msg) throws ServiceException
  {
    if (msg instanceof FileBackedMessage) {
      log.trace("Using FileBackedMessage processing.");
      FileBackedMessage fbm = (FileBackedMessage)msg;
      try (ZipFile zipFile = new ZipFile(fbm.currentSource())){
        Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
          ZipArchiveEntry entry = entries.nextElement();
          if (entry.isDirectory()) {
            continue;
          }
          String entryName = entry.getName();
          if (getFilenameMatch() != null){
            Pattern pattern = Pattern.compile(getFilenameMatch());
            if (!pattern.matcher(entryName).matches()){
              log.trace("The entry [{}] does not match filenameMatch [{}] skipping", entryName, getFilenameMatch());
              continue;
            }
          }
          log.debug("Setting payload using entry [{}] from zip.", entryName);
          try(InputStream is = zipFile.getInputStream(entry)) {
            try (OutputStream os = msg.getOutputStream()) {
              IOUtils.copy(is, os);
            }
          }
          break;
        }
      } catch (IOException ex){
        throw ExceptionHelper.wrapServiceException("Failed to process zip", ex);
      }
    } else {
      log.trace("Message isn't a FileBackedMessage message will not be processed as zip");
    }
  }

  @Override
  public void prepare() throws CoreException
  {
    /* empty method */
  }

  @Override
  protected void initService() throws CoreException
  {
    /* empty method */
  }

  @Override
  protected void closeService()
  {
    /* empty method */
  }

  public String getFilenameMatch() {
    return filenameMatch;
  }

  public void setFilenameMatch(String filenameMatch) {
    this.filenameMatch = filenameMatch;
  }
}

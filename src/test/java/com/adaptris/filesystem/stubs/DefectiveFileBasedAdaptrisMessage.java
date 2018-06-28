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

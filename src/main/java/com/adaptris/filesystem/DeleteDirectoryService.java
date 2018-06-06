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
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.hibernate.validator.constraints.NotBlank;

import java.io.File;

/**
 * @config delete-directory-service
 */
@XStreamAlias("delete-directory-service")
@AdapterComponent
@ComponentProfile(summary = "Will delete directory and its contents.", tag = "FS, FileSystem")
public class DeleteDirectoryService  extends ServiceImp {

  /**
   * The folder to delete.
   */
  @NotBlank
  @InputFieldHint(expression = true)
  private String directoryPath;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    File directory = new File(msg.resolve(getDirectoryPath()));
    if(directory.listFiles() != null) {
      for (final File f : directory.listFiles()) {
        f.delete();
      }
    }
    directory.delete();
  }

  public String getDirectoryPath() {
    return directoryPath;
  }

  public void setDirectoryPath(String directoryPath) {
    this.directoryPath = Args.notEmpty(directoryPath, "directoryPath");
  }

  public DeleteDirectoryService withDirectoryPath(String directoryPath){
    setDirectoryPath(directoryPath);
    return this;
  }

  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {

  }
}

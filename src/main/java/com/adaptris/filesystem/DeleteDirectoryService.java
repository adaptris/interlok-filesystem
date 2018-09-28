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
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
  @InputFieldHint(expression = true)
  @Deprecated
  private String directoryPath;

  @NotNull
  @Valid
  private DataInputParameter<String> path;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      File directory = new File(getPath().extract(msg));
      if (directory.listFiles() != null) {
        for (final File f : directory.listFiles()) {
          f.delete();
        }
      }
      directory.delete();
    } catch (InterlokException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Deprecated
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

  public DataInputParameter<String> getPath() {
    return path;
  }

  public void setPath(DataInputParameter<String> path) {
    this.path = Args.notNull(path, "path");
  }

  public DeleteDirectoryService withPath(DataInputParameter<String> path){
    setPath(path);
    return this;
  }

  @Override
  protected void initService() throws CoreException {
    if(getDirectoryPath() != null){
      log.warn("directoryPath is deprecated use path.");
      if(getPath() == null){
        setPath(new ConstantDataInputParameter(getDirectoryPath()));
      } else {
        log.warn("directoryPath ignored as path is set");
      }
    }
  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {

  }
}

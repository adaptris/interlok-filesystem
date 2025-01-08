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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @config delete-directory-service
 */
@XStreamAlias("delete-directory-service")
@AdapterComponent
@ComponentProfile(summary = "Will delete directory and its contents.", tag = "FS, FileSystem")
public class DeleteDirectoryService  extends ServiceImp {

  @NotNull
  @Valid
  private DataInputParameter<String> path;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      String path = getPath().extract(msg);
      File directory = FsHelper.toFile(path, new File(path));
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

  }

  @Override
  protected void closeService() {

  }

  @Override
  public void prepare() throws CoreException {

  }
}

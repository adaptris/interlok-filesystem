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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @config delete-file-service
 */
@XStreamAlias("delete-file-service")
@AdapterComponent
@ComponentProfile(summary = "Will delete a file", tag = "FS, FileSystem")
public class DeleteFileService extends ServiceImp {

  /**
   * The folder to delete.
   */
  @NotNull
  @Valid
  private DataInputParameter<String> path;

  private Boolean deleteEmptyParent;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      File file = new File(getPath().extract(msg));
      if (file.delete()) {
        File parent = file.getParentFile();
        if (deleteEmptyParent() && parent.isDirectory()) {
          if (parent.list().length == 0) {
            parent.delete();
          }
        }
      }
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

  public Boolean getDeleteEmptyParent() {
    return deleteEmptyParent;
  }

  public void setDeleteEmptyParent(Boolean deleteEmptyParent) {
    this.deleteEmptyParent = deleteEmptyParent;
  }

  private boolean deleteEmptyParent(){
    return getDeleteEmptyParent() != null ? getDeleteEmptyParent() : false;
  }

  public DeleteFileService withPath(DataInputParameter<String> path){
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

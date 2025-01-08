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
import java.io.IOException;
import java.nio.file.Files;
import javax.validation.constraints.NotNull;

import com.adaptris.core.fs.FsHelper;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Move files and / or directories to "new-path"
 *
 * @config move-file-service
 */
@XStreamAlias("move-file-service")
@AdapterComponent
@ComponentProfile(summary = "Will move a files and/or directories to \"new-path\"", tag = "FS, FileSystem")
public class MoveFileService extends ServiceImp {

  @NotNull
  @InputFieldHint(expression = true)
  private String originalPath;

  @NotNull
  @InputFieldHint(expression = true)
  private String newPath;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean moveDirectory;

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    String path = message.resolve(getOriginalPath());
    File originalFile = FsHelper.toFile(path, new File(path));
    if(originalFile.exists() && (moveDirectory() && originalFile.isDirectory() || originalFile.isFile())){
      String newPath = message.resolve(getNewPath());
      File newFile = FsHelper.toFile(newPath, new File(newPath));
      if (!newFile.exists()) {
        try {
          Files.move(originalFile.toPath(), newFile.toPath());
        } catch (IOException e) {
          throw new ServiceException(e);
        }
      } else {
        log.warn("Target {} already exists, cannot move it.", newFile.getAbsolutePath());
      }

    } else {
      log.warn("{} does not exist, cannot move it.", originalFile.getAbsolutePath());
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  public String getOriginalPath() {
    return originalPath;
  }

  public void setOriginalPath(String originalPath) {
    this.originalPath = originalPath;
  }

  public MoveFileService withOriginalPath(String originalPath){
    setOriginalPath(originalPath);
    return this;
  }

  public String getNewPath() {
    return newPath;
  }

  public void setNewPath(String newPath) {
    this.newPath = newPath;
  }

  public MoveFileService withNewPath(String newPath){
    setNewPath(newPath);
    return this;
  }

  public Boolean getMoveDirectory() {
    return moveDirectory;
  }

  public void setMoveDirectory(Boolean moveDirectory) {
    this.moveDirectory = moveDirectory;
  }
 
  public MoveFileService withMoveDirectory(Boolean moveDirectory){
    setMoveDirectory(moveDirectory);
    return this;
  }

  public boolean moveDirectory() {
    return BooleanUtils.toBooleanDefaultIfNull(getMoveDirectory(), false);
  }
  
}

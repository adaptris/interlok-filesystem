package com.adaptris.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("move-directory-service")
@AdapterComponent
@ComponentProfile(summary = "Will move a directory and it's contents to the configured \"new-directory-path\"", tag = "FS, FileSystem")
public class MoveDirectoryService extends ServiceImp {
  
  @NotNull
  private String originalDirectoryPath;
  
  @NotNull
  private String newDirectoryPath;

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    File originalDirFile = new File(message.resolve(this.getOriginalDirectoryPath()));
    
    if(originalDirFile.exists() && originalDirFile.isDirectory()) {
      File newDirFile = new File(message.resolve(this.getNewDirectoryPath()));
      
      if(!newDirFile.exists()) {
        try {
          Files.move(originalDirFile.toPath(), newDirFile.toPath());
        } catch (IOException e) {
          throw new ServiceException(e);
        }
      } else
        log.warn("Target directory {} already exists, cannot move it.", newDirFile.getAbsolutePath());
    } else
      log.warn("Directory {} does not exist, cannot move it.", originalDirFile.getAbsolutePath());
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

  public String getOriginalDirectoryPath() {
    return originalDirectoryPath;
  }

  public void setOriginalDirectoryPath(String originalDirectoryPath) {
    this.originalDirectoryPath = originalDirectoryPath;
  }

  public String getNewDirectoryPath() {
    return newDirectoryPath;
  }

  public void setNewDirectoryPath(String newDirectoryPath) {
    this.newDirectoryPath = newDirectoryPath;
  }
  
  public static void main(String[] args) {
    MoveDirectoryService moveDirectoryService = new MoveDirectoryService();
    moveDirectoryService.setOriginalDirectoryPath("C:\\Adaptris\\3.6.5\\ui-resources");
    moveDirectoryService.setNewDirectoryPath("C:\\Adaptris\\3.6.5\\ui-resources2");
    
    try {
      moveDirectoryService.doService(DefaultMessageFactory.getDefaultInstance().newMessage());
    } catch (ServiceException e) {
      e.printStackTrace();
    }
    
  }

}

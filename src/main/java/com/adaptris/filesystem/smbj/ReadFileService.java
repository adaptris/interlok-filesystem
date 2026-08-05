/*******************************************************************************
 * Copyright 2020 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.filesystem.smbj;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Args;
import com.hierynomus.smbj.common.SmbPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Read a file from a remote SMB Share.
 * 
 * @config smb-read-file-service
 *
 */
@XStreamAlias("smb-read-file-service")
@ComponentProfile(summary = "Read a file from SMB and make it the existing payload", tag = "smb,samba", since = "3.10.1",
    recommended = {SMBConnection.class})
@AdapterComponent
@DisplayOrder(order = {"connection", "smbPath"})
public class ReadFileService extends ServiceImp implements ConnectedService {

  /**
   * Set the connection to use when connecting to SMB.
   * 
   */
  @Getter
  @Setter
  @Valid
  @NotNull
  private AdaptrisConnection connection;
  /**
   * The path to where the file will be read from.
   * <p>
   * This should be a UNC style path e.g. {@code \\10.0.0.1\ShareName\path\to\actual\file} or an expression that resolves to a fully
   * qualified path.
   * </p>
   */
  @Getter
  @Setter
  @NotBlank
  @InputFieldHint(expression = true)
  private String smbPath;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      SmbPath smbRef = SmbPath.parse(msg.resolve(getSmbPath()));
      log.trace("Attempting to get [{}]", smbRef.toUncPath());
      Connector connector = getConnection().retrieveConnection(SMBConnection.class).createOrGetWorker(smbRef);
      Helper.read(connector.getDiskShare(), smbRef, msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getConnection());
  }

  @Override
  protected void initService() throws CoreException {
    Args.notNull(getConnection(), "connection");
    Args.notBlank(getSmbPath(), "smb-path");
    LifecycleHelper.init(getConnection());
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getConnection());
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getConnection());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getConnection());
  }

  public ReadFileService withConnection(AdaptrisConnection c) {
    setConnection(c);
    return this;
  }

  public ReadFileService withPath(String s) {
    setSmbPath(s);
    return this;
  }
}

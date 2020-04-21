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

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import com.adaptris.interlok.util.Args;
import com.hierynomus.protocol.commons.IOUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import lombok.Getter;

public class Connector implements Closeable {

  // This is the base item that consumers / producers use.
  @Getter
  private transient DiskShare diskShare;

  private transient Connection connection;
  private transient SMBConnection owner;
  private transient SMBClient client;
  private transient Session session;
  private transient SmbPath serverDetails;

  public Connector(SmbPath path, SMBConnection owner) throws Exception {
    serverDetails = path;
    this.owner = owner;
  }

  public Connector connect(SMBClient c) throws Exception {
    client = Args.notNull(c, "smb-client");
    connection = client.connect(serverDetails.getHostname());
    session = connection.authenticate(owner.authenticationProvider().authContext());
    diskShare = (DiskShare) session.connectShare(serverDetails.getShareName());
    return this;
  }

  @Override
  public void close() throws IOException {
    // use the smbj equiv of IOUtils since these are AutoCloseable not Closeable.
    IOUtils.closeSilently(diskShare, session, connection, client);
    // Set connection to be null, since this has an isConnected...
    connection = null;
  }

  public boolean isConnected() {
    return Optional.ofNullable(connection).map((c) -> c.isConnected()).orElse(false);
  }

}

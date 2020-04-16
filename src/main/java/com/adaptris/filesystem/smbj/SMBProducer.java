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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Optional;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.util.ExceptionHelper;
import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Produce to a SMB share.
 * 
 * 
 * @config smb-producer
 * 
 */
@XStreamAlias("smb-producer")
@ComponentProfile(summary = "Write to an SMB location", tag = "samba,smb", recommended = {SMBConnection.class}, since = "3.10.1")
@DisplayOrder(order = {"destination", "overwrite", "filenameCreator", "encoder", "overwrite"})
@NoArgsConstructor
public class SMBProducer extends ProduceOnlyProducerImp {
  @Valid
  @Getter
  @Setter
  @InputFieldDefault(value = "formatted-filename-creator")
  private FileNameCreator filenameCreator;

  /**
   * Specify the mode in which the write of the file happens.
   * <p>
   * If not specified then the default is {@link CreateMode} which means the produce will fail if the file already exists. We
   * consider this the safest mode since we aren't using intermediate staging files to ensure atomic operations.
   * </p>
   */
  @Getter
  @Setter
  @Valid
  @InputFieldDefault(value = "smb-create-mode")
  private WriteMode mode;

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    try {
      String uncPath = dest.getDestination(msg);
      SmbPath smbRoot = SmbPath.parse(uncPath);
      SmbPath filePath= new SmbPath(smbRoot, filenameCreator().createName(msg));
      SMBConnection conn = retrieveConnection(SMBConnection.class);
      Connector worker = conn.createOrGetWorker(filePath);
      WriteMode mode = mode();
      try (File smbFile = openFile(worker.getDiskShare(), filePath, mode);
          OutputStream out = mode.getOutputStream(smbFile)) {
        outputWriter().write(msg, out);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
  }


  public SMBProducer withFilenameCreator(FileNameCreator f) {
    setFilenameCreator(f);
    return this;
  }

  public SMBProducer withMode(WriteMode m) {
    setMode(m);
    return this;
  }

  private WriteMode mode() {
    return ObjectUtils.defaultIfNull(getMode(), new CreateMode());
  }

  private EncodedWriter outputWriter() {
    return Optional.ofNullable(getEncoder()).map((e) -> encoderWriter()).orElse(rawWriter());
  }

  private FileNameCreator filenameCreator() {
    return ObjectUtils.defaultIfNull(getFilenameCreator(), new FormattedFilenameCreator());
  }

  private static File openFile(DiskShare share, SmbPath filePath, WriteMode mode) {
    return share.openFile(filePath.getPath(), EnumSet.of(AccessMask.GENERIC_ALL), null, SMB2ShareAccess.ALL,
        mode.fileOpenMode(), null);
  }

  private static EncodedWriter rawWriter() {
    return (msg, out) -> {
      try (InputStream in = msg.getInputStream()) {
        IOUtils.copy(in, out);
      }
    };
  }

  private EncodedWriter encoderWriter() {
    return (msg, out) -> {
      getEncoder().writeMessage(msg, out);
    };
  }

  // Probably is just a java.util.BiConsumer really.
  @FunctionalInterface
  private interface EncodedWriter {
    void write(AdaptrisMessage msg, OutputStream out) throws Exception;
  }
}

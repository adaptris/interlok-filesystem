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

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.FileNameCreator;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.ExceptionHelper;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.share.File;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * Produce to a SMB share.
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

  /**
   * The SMB Path to write files to in the form {@code \\server-name\shareName\path\to\dir}.
   *
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  @NotBlank
  private String path;

  @Override
  public void prepare() throws CoreException
  {
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String uncPath) throws ProduceException {
    try {
      SmbPath smbRoot = SmbPath.parse(uncPath);
      SmbPath filePath = new SmbPath(smbRoot, filenameCreator().createName(msg));
      SMBConnection conn = retrieveConnection(SMBConnection.class);
      Connector worker = conn.createOrGetWorker(filePath);
      WriteMode mode = mode();
      try (File smbFile = Helper.open(worker.getDiskShare(), filePath, mode);
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

  public SMBProducer withPath(String s) {
    setPath(s);
    return this;
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return msg.resolve(getPath());
  }

  // Probably is just a java.util.BiConsumer really.
  @FunctionalInterface
  private interface EncodedWriter {
    void write(AdaptrisMessage msg, OutputStream out) throws Exception;
  }

}

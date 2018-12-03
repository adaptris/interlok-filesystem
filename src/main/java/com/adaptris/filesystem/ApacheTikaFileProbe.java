package com.adaptris.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.tika.Tika;

import com.adaptris.core.services.ReadFileService;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * File probe based on <a href="http://tika.apache.org/">Apache Tika</a>.
 * 
 * <p>
 * Since {@link Files#probeContentType(java.nio.file.Path)} is OS and implementation dependent (by default it returns {@code null}
 * on a Mac; this is provided for behaviour consistency using a third party package. It uses {@code Tika#detect(java.nio.file.Path)}
 * to probe the content type of the file.
 * </p>
 * <p>
 * You can optionally include {@code org.apache.tika:tika-parsers} in your dependency tree for parsing based on the actual content
 * for additional file type. If you are including {@code tika-parsers} then you may need to explicitly include a STaX2 parser
 * implementation in your dependency tree; {@code org.apache.cxf:cxf-rt-rs-client} has a dependency on woodstox which will cause
 * XStream to use STaX2.
 * </p>
 * 
 * @config apache-tika-file-probe
 *
 */
@XStreamAlias("apache-tika-file-probe")
public class ApacheTikaFileProbe implements ReadFileService.ContentTypeProbe {

  private transient Tika tika = new Tika();

  @Override
  public String probeContentType(File f) throws IOException {
    return tika.detect(f.toPath());
  }

}

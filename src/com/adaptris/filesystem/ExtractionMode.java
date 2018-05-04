package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

/**
 * @author mwarman
 */
public interface ExtractionMode {

  void extract(TarArchiveInputStream tarArchiveInputStream, AdaptrisMessage adaptrisMessage) throws CoreException;
}

package com.adaptris.filesystem;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;

/**
 * @author mwarman
 */
@XStreamAlias("tar-gzip-unarchiver-service")
public class TarGZipUnArchiverService extends ServiceImp {

  private ExtractionMode extractionMode;

  public TarGZipUnArchiverService(){
    this(new DirectoryExtractionMode());
  }

  public TarGZipUnArchiverService(ExtractionMode extractionMode){
    setExtractionMode(extractionMode);
  }

  @Override
  public void doService(AdaptrisMessage adaptrisMessage) throws ServiceException {
    try (
        GzipCompressorInputStream gzIn = new GzipCompressorInputStream(adaptrisMessage.getInputStream());
        TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn)){
      getExtractionMode().extract(tarIn, adaptrisMessage);
    } catch (CoreException | IOException e) {
      ExceptionHelper.rethrowServiceException(e.getMessage(), e);
    }
  }

  public ExtractionMode getExtractionMode() {
    return extractionMode;
  }

  public void setExtractionMode(ExtractionMode extractionMode) {
    this.extractionMode = extractionMode;
  }

  public TarGZipUnArchiverService withExtractionMode(ExtractionMode extractionMode){
    setExtractionMode(extractionMode);
    return this;
  }

  @Override
  protected void initService() throws CoreException {

  }

  @Override
  public void prepare() throws CoreException {

  }

  @Override
  protected void closeService() {

  }
}

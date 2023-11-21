package com.adaptris.filesystem;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author mwarman
 * @config commons-io-directory-listing-provider
 */
@XStreamAlias("commons-io-directory-listing-provider")
public class DirectoryListingProviderCommonsIO implements DirectoryListingProvider {

  @InputFieldDefault(value = "false")
  private Boolean recursive;

  public DirectoryListingProviderCommonsIO() {
  }

  public DirectoryListingProviderCommonsIO(Boolean recursive) {
    setRecursive(recursive);
  }

  @Override
  public List<File> getFiles(File directory) {
    return (List<File>) FileUtils.listFiles(directory, null, recursive());
  }

  public Boolean getRecursive() {
    return recursive;
  }

  public void setRecursive(Boolean recursive) {
    this.recursive = Args.notNull(recursive, "recursive");
  }

  private boolean recursive() {
    return getRecursive() != null ? getRecursive() : false;
  }

}

package com.adaptris.filesystem;

import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author mwarman
 */
@XStreamAlias("commons-io-directory-listing-provider")
public class DirectoryListingProviderCommonsIO implements DirectoryListingProvider{

  private Boolean recursive;

  public DirectoryListingProviderCommonsIO(){

  }

  public DirectoryListingProviderCommonsIO(Boolean recursive){
    setRecursive(recursive);
  }

  @Override
  public List<File> getFiles(File directory) {
    return (List<File>)FileUtils.listFiles(directory, null, recursive());
  }

  public Boolean getRecursive() {
    return recursive;
  }

  public void setRecursive(Boolean recursive) {
    this.recursive = Args.notNull(recursive, "recursive");
  }

  private boolean recursive(){
    return getRecursive() != null ? getRecursive() : false;
  }
}

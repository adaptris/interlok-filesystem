package com.adaptris.filesystem;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author mwarman
 */
@XStreamAlias("default-directory-listing-provider")
public class DirectoryListingProviderDefault implements DirectoryListingProvider{

  @Override
  public List<File> getFiles(File directory) {
    return Arrays.asList(directory.listFiles());
  }
}

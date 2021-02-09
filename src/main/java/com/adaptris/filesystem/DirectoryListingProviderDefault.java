package com.adaptris.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author mwarman
 * @config default-directory-listing-provider
 */
@XStreamAlias("default-directory-listing-provider")
public class DirectoryListingProviderDefault implements DirectoryListingProvider{

  @Override
  public List<File> getFiles(File directory) {
    return Arrays.asList(directory.listFiles());
  }
}

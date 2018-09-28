package com.adaptris.filesystem;

import java.io.File;
import java.util.List;

/**
 * @author mwarman
 */
public interface DirectoryListingProvider {

  List<File> getFiles(File directory);
}

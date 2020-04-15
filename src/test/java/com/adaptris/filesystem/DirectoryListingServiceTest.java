/*
 * Copyright Adaptris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adaptris.filesystem;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.services.metadata.DateFormatBuilder;

public class DirectoryListingServiceTest extends ServiceCase {
  private static final String METADATA_KEY = "service-test";
  private static final List<String> DEFAULT_PROVIDER_EXPECTED_FILES = Arrays.asList("text1.xml", "text2.xml", "recursive");
  private static final List<String> COMMONS_IO_PROVIDER_EXPECTED_FILES = Arrays.asList("text1.xml", "text2.xml");
  private static final List<String> COMMONS_IO_PROVIDER_EXPECTED_FILES_RECURSIVE =
      Arrays.asList("text1.xml", "text2.xml", "text3.xml");
  private String directoryPath;

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    directoryPath = createTempDirectory().getAbsolutePath();
  }

  @After
  public void tearDown() throws Exception {
    cleanUpTempDirectory(new File(directoryPath));
  }

  @Test
  public void testServiceWithMetadataKey() throws Exception {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    final DirectoryListingService service = new DirectoryListingService();
    service.setDirectory(new ConstantDataInputParameter(directoryPath));
    service.setOutput(new MetadataDataOutputParameter(METADATA_KEY));

    execute(service, message);

    final String ls = message.getMetadataValue(METADATA_KEY);

    for (final String file : DEFAULT_PROVIDER_EXPECTED_FILES) {
      assertTrue(ls.contains(file));
    }
  }

  @Test
  public void testServiceCommonsIO() throws Exception {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    final DirectoryListingService service = new DirectoryListingService();
    service.setDirectory(new ConstantDataInputParameter(directoryPath));
    service.setOutput(new StringPayloadDataOutputParameter());
    service.setDirectoryListingProvider(new DirectoryListingProviderCommonsIO());

    execute(service, message);

    final String ls = message.getContent();

    for (final String file : COMMONS_IO_PROVIDER_EXPECTED_FILES) {
      assertTrue(ls.contains(file));
    }
  }

  @Test
  public void testServiceCommonsIORecursive() throws Exception {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    final DirectoryListingService service = new DirectoryListingService();
    service.setDirectory(new ConstantDataInputParameter(directoryPath));
    service.setOutput(new StringPayloadDataOutputParameter());
    service.setDirectoryListingProvider(new DirectoryListingProviderCommonsIO(true));

    execute(service, message);

    final String ls = message.getContent();

    for (final String file : COMMONS_IO_PROVIDER_EXPECTED_FILES_RECURSIVE) {
      assertTrue(ls.contains(file));
    }
  }

  @Test
  public void testServiceWithDateFormat() throws Exception {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    final DirectoryListingService service = new DirectoryListingService();
    service.setDirectory(new ConstantDataInputParameter(directoryPath));
    service.setOutput(new MetadataDataOutputParameter(METADATA_KEY));
    service.setDateFormatBuilder(new DateFormatBuilder("yyyy-MM-dd"));

    execute(service, message);

    final String ls = message.getMetadataValue(METADATA_KEY);
    for (final String file : DEFAULT_PROVIDER_EXPECTED_FILES) {
      assertTrue(ls.contains(file));
    }
    Pattern p = Pattern.compile("\"\\d{4}-\\d{2}-\\d{2}\"");
    Matcher m = p.matcher(ls);
    assertTrue(m.find());

  }

  @Test
  public void testServicePayload() throws Exception {
    final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    final DirectoryListingService service = new DirectoryListingService();
    service.setDirectory(new ConstantDataInputParameter(directoryPath));
    service.setOutput(new StringPayloadDataOutputParameter());

    execute(service, message);

    final String ls = message.getContent();

    for (final String file : DEFAULT_PROVIDER_EXPECTED_FILES) {
      assertTrue(ls.contains(file));
    }
  }


  public File createTempDirectory() throws IOException {
    File tempDir = File.createTempFile(DirectoryListingProvider.class.getSimpleName(), "", null);
    tempDir.delete();
    if (!tempDir.exists()) {
      tempDir.mkdir();
    }
    new FileOutputStream(new File(tempDir, "text1.xml")).close();
    new FileOutputStream(new File(tempDir, "text2.xml")).close();
    File recursive = new File(tempDir, "recursive");
    recursive.mkdir();
    new FileOutputStream(new File(recursive, "text3.xml")).close();
    return tempDir;
  }

  public void cleanUpTempDirectory(File tempDir) {
    for (final File f : FileUtils.listFiles(tempDir, null, true)) {
      f.delete();
    }
    File recursive = new File(tempDir, "recursive");
    recursive.delete();
    tempDir.delete();
  }

  @Override
  protected DirectoryListingService retrieveObjectForSampleConfig() {
    final DirectoryListingService service = new DirectoryListingService();
    service.setDirectory(new ConstantDataInputParameter("/opt/data/files"));
    service.setOutput(new MetadataDataOutputParameter(METADATA_KEY));
    return service;
  }
}

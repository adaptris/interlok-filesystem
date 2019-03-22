/*
 * Copyright Adaptris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.filesystem;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipFolderTest {
  private File tmpDir;

  @Before
  public void init() {
    tmpDir = new File(System.getProperty("java.io.tmpdir"), "ZipFolderTest");
    if (!tmpDir.exists()) {
      tmpDir.mkdir();
    }
  }

  @After
  public void deinit() {
    for (final File f : tmpDir.listFiles()) {
      f.delete();
    }
    tmpDir.delete();
  }

  @Test
  public void test1() throws Exception {
    ZipFolder zf = new ZipFolder("build.gradle");
    final byte[] z = zf.zip();

    final File zip = new File(tmpDir, "build.zip");
    try (final FileOutputStream fos = new FileOutputStream(zip)) {
      fos.write(z);
    }

    zf = new ZipFolder(tmpDir.getAbsolutePath());
    try (final FileInputStream fis = new FileInputStream(zip)) {
      zf.unzip(fis);
    }
  }

  @Test
  public void test2() throws Exception {
    ZipFolder zf = new ZipFolder(new File("build.gradle").getAbsolutePath());
    final byte[] z = zf.zip();

    final File zip = new File(tmpDir, "build.zip");
    try (final FileOutputStream fos = new FileOutputStream(zip)) {
      fos.write(z);
    }

    zf = new ZipFolder(tmpDir.getAbsolutePath());
    try (final FileInputStream fis = new FileInputStream(zip)) {
      zf.unzip(fis);
    }
  }

  @Test
  public void testValidateTree() throws Exception {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File child = new File(tempDir, ZipFolderTest.class.getCanonicalName());
    assertEquals(child, ZipFolder.validateTree(tempDir, child));
  }

  @Test(expected = IOException.class)
  public void testValidateTree_Failure() throws Exception {
    File tempDir = new File(System.getProperty("java.io.tmpdir"));
    File child = new File(tempDir, "../../" + ZipFolderTest.class.getCanonicalName());
    ZipFolder.validateTree(tempDir, child);
  }
}

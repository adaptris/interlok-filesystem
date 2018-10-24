/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.filesystem;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.w3c.dom.Document;

import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

public class ApacheTikaFileProbeTest {

  @Test
  public void testProbe() throws Exception {
    File tarGz = new File(this.getClass().getClassLoader().getResource("archive.tar.gz").getFile());
    ApacheTikaFileProbe probe = new ApacheTikaFileProbe();
    String type = probe.probeContentType(tarGz);
    // System.out.println(type);
    assertTrue(type.toLowerCase().contains("gzip"));
    File xml = generateXML();
    type = probe.probeContentType(xml);
    // System.out.println(type);
    assertTrue(type.toLowerCase().contains("xml"));
  }

  private File generateXML() throws Exception {
    File f = TempFileUtils.createTrackedFile(this);
    // Do this to make sure we end up with a <?xml version="1.0"?> directive...
    Document d = XmlHelper.createDocument(DefaultMarshaller.getDefaultMarshaller().marshal(new UnzipService()),
        DocumentBuilderFactoryBuilder.newInstance());
    try (OutputStream out = new FileOutputStream(f)) {
      XmlUtils util = new XmlUtils();
      util.setSource(d);
      util.writeDocument(out, "UTF-8");
    }
    return f;
  }

}

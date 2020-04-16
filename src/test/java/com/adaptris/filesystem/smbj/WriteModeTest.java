package com.adaptris.filesystem.smbj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import org.mockito.Mockito;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.smbj.share.File;

public class WriteModeTest {

  @Test
  public void testCreateMode_OpenMode() throws Exception {
    assertEquals(SMB2CreateDisposition.FILE_CREATE, new CreateMode().fileOpenMode());
  }

  @Test
  public void testCreateMode_GetOutputStream() throws Exception {
    File smbFile = mockFile();
    assertNotNull(new CreateMode().getOutputStream(smbFile));
  }


  @Test
  public void testOverwriteMode_OpenMode() throws Exception {
    assertEquals(SMB2CreateDisposition.FILE_OVERWRITE_IF, new OverwriteMode().fileOpenMode());
  }

  @Test
  public void testOverwriteMode_GetOutputStream() throws Exception {
    File smbFile = mockFile();
    assertNotNull(new OverwriteMode().getOutputStream(smbFile));
  }


  @Test
  public void testAppendMode_OpenMode() throws Exception {
    assertEquals(SMB2CreateDisposition.FILE_OPEN_IF, new AppendMode().fileOpenMode());
  }

  @Test
  public void testAppendMode_GetOutputStream() throws Exception {
    File smbFile = mockFile();
    assertNotNull(new AppendMode().getOutputStream(smbFile));
  }

  private File mockFile() {
    File mockFile = Mockito.mock(File.class);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    Mockito.when(mockFile.getOutputStream()).thenReturn(output);
    Mockito.when(mockFile.getOutputStream(anyBoolean())).thenReturn(output);
    return mockFile;
  }
}

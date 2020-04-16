package com.adaptris.filesystem.smbj;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import org.junit.Test;

public class AuthenticationProviderTest {

  @Test
  public void testAnonymous() throws Exception {
    assertNotNull(new AnonymousAccess().authContext());
  }

  @Test
  public void testGuest() throws Exception {
    assertNotNull(new GuestAccess().authContext());
  }

  @Test
  public void testUserAccess() throws Exception {
    UserAccess user = new UserAccess();
    assertNotNull(user.authContext());
    user.withDomain("domain").withUser("user").withPassword("password");
    assertNotSame(user.authContext(), user.authContext());
  }
}

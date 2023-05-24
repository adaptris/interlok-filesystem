/*******************************************************************************
 * Copyright 2020 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.filesystem.smbj;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import org.junit.jupiter.api.Test;

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

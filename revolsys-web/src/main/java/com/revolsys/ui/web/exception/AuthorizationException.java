/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.web.exception;

/**
 * The AuthorizationException should be thrown if the user does not have
 * permission to access the page.
 *
 * @author P.D.Austin
 * @version 1.0
 */
public class AuthorizationException extends ActionException {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -1543147341720847543L;

  /**
   * Construct a new AuthenticationException.
   */
  public AuthorizationException() {
  }

  /**
   * Construct a new AuthenticationException with the specified message.
   *
   * @param message The reason the exception was thrown
   */
  public AuthorizationException(final String message) {
    super(message);
  }
}

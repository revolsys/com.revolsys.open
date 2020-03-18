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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The PageNotFoundException should be thrown if not enough information can be
 * obtained to generate a page. This would occur if the content item did not
 * exist. The result of throwing this exception will be a 404 error being
 * returned to the user.
 *
 * @author P.D.Austin
 * @version 1.0
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PageNotFoundException extends RuntimeException {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = 1L;

  /**
   * Construct a new PageNotFoundException.
   */
  public PageNotFoundException() {
  }

  /**
   * Construct a new PageNotFoundException with the specified message.
   *
   * @param message The reason the exception was thrown
   */
  public PageNotFoundException(final String message) {
    super(message);
  }
}

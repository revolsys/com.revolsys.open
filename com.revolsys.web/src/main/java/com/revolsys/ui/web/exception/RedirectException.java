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
 * The RedirectException can be used to redirect a user to a specified url. This
 * is used instead of sendRedirect on the ServletResponse.
 * 
 * @author P.D.Austin
 * @version 1.0
 */
public class RedirectException extends ActionException {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -8661796861568286704L;

  /**
   * Construct a new RedirectException.
   */
  public RedirectException() {
  }

  /**
   * Construct a new PageNotFoundException with the specified message.
   * 
   * @param message The reason the exception was thrown
   */
  public RedirectException(final String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  private String url;
}

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

import javax.servlet.ServletException;

/**
 * The ActionException is the super class of all exceptions thrown by process
 * method of the IafAction class.
 *
 * @see com.revolsys.ui.web.config.IafAction#process
 * @version 1.0
 */
public class ActionException extends ServletException {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -8376527490107349679L;

  /**
   * Construct a new ActionException.
   */
  public ActionException() {
  }

  /**
   * Construct a new ActionException with the specified message.
   *
   * @param message The reason the exception was thrown
   */
  public ActionException(final String message) {
    super(message);
  }

  /**
   * Construct a new ActionException with an original Exception and the
   * specified message. This should be used to propagate the original exception.
   *
   * @param message The reason the exception was thrown
   * @param rootCause The original exception that was thrown
   */
  public ActionException(final String message, final Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Construct a new ActionException with an original Exception. This should be
   * used to propagate the original exception.
   *
   * @param rootCause The original exception that was thrown
   */
  public ActionException(final Throwable rootCause) {
    super(rootCause);
  }
}

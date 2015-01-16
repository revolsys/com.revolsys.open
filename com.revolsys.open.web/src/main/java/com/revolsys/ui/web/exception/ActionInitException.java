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
 * The ActionInitException is the super class of all exceptions thrown by init
 * method of the IafAction class.
 *
 * @version 1.0
 */
public class ActionInitException extends ActionException {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -1245197703059190582L;

  /**
   * Construct a new ActionInitException.
   */
  public ActionInitException() {
  }

  /**
   * Construct a new ActionInitException with the specified message.
   *
   * @param message The reason the exception was thrown
   */
  public ActionInitException(final String message) {
    super(message);
  }

  /**
   * Construct a new ActionInitException with an original Exception and the
   * specified message. This should be used to propagate the original exception.
   *
   * @param message The reason the exception was thrown
   * @param rootCause The original exception that was thrown
   */
  public ActionInitException(final String message, final Throwable rootCause) {
    super(message, rootCause);
  }

  /**
   * Construct a new ActionInitException with an original Exception. This should
   * be used to propagate the original exception.
   *
   * @param rootCause The original exception that was thrown
   */
  public ActionInitException(final Throwable rootCause) {
    super(rootCause);
  }
}

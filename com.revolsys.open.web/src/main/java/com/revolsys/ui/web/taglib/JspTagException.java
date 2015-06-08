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
package com.revolsys.ui.web.taglib;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.jsp.JspException;

/**
 * The JspTagException should be thrown if there was an error generated in a tag
 * library. This exception supports wrapping of a nested exception.
 *
 * @author P.D.Austin
 * @version 1.0
 */
public class JspTagException extends JspException {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -566710852809417270L;

  private Throwable rootCause;

  /**
   * Construct a new JspTagException.
   */
  public JspTagException() {
    super("");
  }

  /**
   * Construct a new JspTagException with the specified message.
   *
   * @param message The reason the exception was thrown
   */
  public JspTagException(final String message) {
    super(message);
  }

  /**
   * Construct a new JspTagException with an original Exception and the
   * specified message. This should be used to propagate the original exception.
   *
   * @param message The reason the exception was thrown
   * @param rootCause The original exception that was thrown
   */
  public JspTagException(final String message, final Throwable rootCause) {
    super(message);
    this.rootCause = rootCause;
  }

  /**
   * Construct a new JspTagException with an original Exception. This should be
   * used to propagate the original exception.
   *
   * @param rootCause The original exception that was thrown
   */
  public JspTagException(final Throwable rootCause) {
    super(rootCause.getMessage());
    this.rootCause = rootCause;
  }

  /**
   * Returns the detail message, including the message from the nested exception
   * if there is one.
   *
   * @return the detail message
   */
  @Override
  public String getMessage() {
    if (this.rootCause == null) {
      return super.getMessage();
    } else {
      return new StringBuilder(super.getMessage()).append("; nested exception is: \n\t")
        .append(this.rootCause)
        .toString();
    }
  }

  /**
   * Prints the composite message to <code>System.err</code>.
   */
  @Override
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  /**
   * Prints the composite message and the embedded stack trace to the specified
   * stream <code>ps</code>.
   *
   * @param ps the print stream
   */
  @Override
  public void printStackTrace(final PrintStream ps) {
    if (this.rootCause == null) {
      super.printStackTrace(ps);
    } else {
      synchronized (ps) {
        ps.println(this);
        this.rootCause.printStackTrace(ps);
      }
    }
  }

  /**
   * Prints the composite message and the embedded stack trace to the specified
   * print writer <code>pw</code>.
   *
   * @param pw the print writer
   */
  @Override
  public void printStackTrace(final PrintWriter pw) {
    if (this.rootCause == null) {
      super.printStackTrace(pw);
    } else {
      synchronized (pw) {
        pw.println(this);
        this.rootCause.printStackTrace(pw);
      }
    }
  }
}

package com.revolsys.util;

public class WrappedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public WrappedException(final Throwable cause) {
    super(cause);
  }

}

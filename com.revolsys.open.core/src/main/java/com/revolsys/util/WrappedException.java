package com.revolsys.util;

public class WrappedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public WrappedException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public WrappedException(final Throwable cause) {
    super(cause);
  }
}

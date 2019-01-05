package com.revolsys.data.exception;

public class DataAccessException extends RuntimeException {

  public DataAccessException(final String message) {
    super(message);
  }

  public DataAccessException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public DataAccessException(final Throwable cause) {
    super(cause);
  }

}

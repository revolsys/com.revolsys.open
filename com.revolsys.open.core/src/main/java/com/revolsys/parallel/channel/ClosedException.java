package com.revolsys.parallel.channel;

@SuppressWarnings("serial")
public class ClosedException extends RuntimeException {

  public ClosedException() {
    super();
  }

  public ClosedException(String message, Throwable cause) {
    super(message, cause);
  }

  public ClosedException(String message) {
    super(message);
  }

  public ClosedException(Throwable cause) {
    super(cause);
  }

}

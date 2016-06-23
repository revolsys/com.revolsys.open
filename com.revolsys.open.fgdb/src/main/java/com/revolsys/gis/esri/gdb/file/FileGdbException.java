package com.revolsys.gis.esri.gdb.file;

public class FileGdbException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public FileGdbException() {
  }

  public FileGdbException(final String message) {
    super(message);
  }

  public FileGdbException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public FileGdbException(final Throwable cause) {
    super(cause);
  }
}

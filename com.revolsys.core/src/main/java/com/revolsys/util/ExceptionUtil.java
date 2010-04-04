package com.revolsys.util;

import java.lang.reflect.InvocationTargetException;

public final class ExceptionUtil {
  public static <T> T throwCauseException(
    final Throwable e) {
    final Throwable cause = e.getCause();
    return (T)throwUncheckedException(cause);
  }

  public static <T> T throwUncheckedException(
    final Throwable exception) {
    if (exception instanceof InvocationTargetException) {
      return (T)throwCauseException(exception);
    } else if (exception instanceof RuntimeException) {
      throw (RuntimeException)exception;
    } else if (exception instanceof Error) {
      throw (Error)exception;
    } else {
      throw new RuntimeException(exception);
    }
  }
}

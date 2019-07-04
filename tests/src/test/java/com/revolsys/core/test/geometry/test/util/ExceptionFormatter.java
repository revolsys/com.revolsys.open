package com.revolsys.core.test.geometry.test.util;

public class ExceptionFormatter {

  public static String getFullString(final Throwable ex) {
    return ex.getClass().getName() + " : " + ex.toString();
  }

}

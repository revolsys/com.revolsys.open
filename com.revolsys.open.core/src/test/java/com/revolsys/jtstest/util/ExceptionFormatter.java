package com.revolsys.jtstest.util;

public class ExceptionFormatter {

  public static String getFullString(Throwable ex)
  {
    return ex.getClass().getName() + " : " + ex.toString();
  }

}

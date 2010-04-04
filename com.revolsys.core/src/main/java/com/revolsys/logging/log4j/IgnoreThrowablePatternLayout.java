package com.revolsys.logging.log4j;

import org.apache.log4j.PatternLayout;

public class IgnoreThrowablePatternLayout extends PatternLayout {
  @Override
  public boolean ignoresThrowable() {
    return false;
  }
}

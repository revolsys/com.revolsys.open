package com.revolsys.logging.log4j;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class WrappedAppender extends AppenderSkeleton {

  private final Appender appender;

  public WrappedAppender(final Appender appender) {
    this.appender = appender;
  }

  @Override
  protected void append(final LoggingEvent event) {
    this.appender.doAppend(event);
  }

  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return this.appender.requiresLayout();
  }

}

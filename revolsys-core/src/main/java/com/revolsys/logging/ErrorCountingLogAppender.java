package com.revolsys.logging;

import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.log.LogbackUtil;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * This is a logging appender that counts the number
 * of times an ERROR (or higher) logging event is recorded.
 */
public class ErrorCountingLogAppender extends AppenderBase<ILoggingEvent> {

  private final AtomicInteger count = new AtomicInteger();

  public ErrorCountingLogAppender() {
    super.setName("Error Counting Log Appender");
    super.setContext(LogbackUtil.getRootLogger().getLoggerContext());
  }

  @Override
  protected void append(final ILoggingEvent event) {

    if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
      this.count.incrementAndGet();
    }

  }

  public int getErrorCount() {
    return this.count.get();
  }

}

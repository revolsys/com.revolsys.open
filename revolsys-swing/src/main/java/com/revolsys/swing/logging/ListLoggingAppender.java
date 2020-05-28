package com.revolsys.swing.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class ListLoggingAppender extends AppenderBase<ILoggingEvent> {
  private static int index = 0;

  private final LoggingTableModel tableModel;

  public ListLoggingAppender(final LoggingTableModel tableModel) {
    synchronized (getClass()) {
      setName(getClass().getName() + "-" + index++);
    }
    this.tableModel = tableModel;
  }

  @Override
  protected void append(final ILoggingEvent event) {
    this.tableModel.addLoggingEvent(event);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ListLoggingAppender) {
      final ListLoggingAppender appender = (ListLoggingAppender)obj;
      return this.tableModel == appender.tableModel;
    }

    return false;
  }

  @Override
  public void stop() {
    super.stop();
    this.tableModel.clear();
  }
}

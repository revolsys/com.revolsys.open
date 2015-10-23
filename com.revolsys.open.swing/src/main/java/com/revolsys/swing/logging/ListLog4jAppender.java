package com.revolsys.swing.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class ListLog4jAppender extends AppenderSkeleton {
  private final Log4jTableModel tableModel;

  public ListLog4jAppender(final Log4jTableModel tableModel) {
    this.tableModel = tableModel;
  }

  @Override
  protected void append(final LoggingEvent event) {
    this.tableModel.addLoggingEvent(event);
  }

  @Override
  public void close() {
    this.tableModel.clear();
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }
}

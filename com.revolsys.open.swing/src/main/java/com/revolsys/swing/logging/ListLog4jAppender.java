package com.revolsys.swing.logging;

import org.apache.logging.log4j.core.LogEvent;

public class ListLog4jAppender extends BaseAppender {
  private final Log4jTableModel tableModel;

  public ListLog4jAppender(final Log4jTableModel tableModel) {
    setName("table");
    this.tableModel = tableModel;
  }

  @Override
  public void append(final LogEvent event) {
    this.tableModel.addLoggingEvent(event);
  }

  @Override
  public void stop() {
    super.stop();
    this.tableModel.clear();
  }
}

package com.revolsys.swing.logging;

import org.apache.logging.log4j.core.LogEvent;

public class ListLog4jAppender extends BaseAppender {
  private static int index = 0;

  private final Log4jTableModel tableModel;

  public ListLog4jAppender(final Log4jTableModel tableModel) {
    synchronized (getClass()) {
      setName(getClass().getName() + "-" + index++);
    }
    this.tableModel = tableModel;
  }

  @Override
  public void append(final LogEvent event) {
    this.tableModel.addLoggingEvent(event);
  }

  @Override
  protected boolean equalsImpl(final Object obj) {
    if (super.equalsImpl(obj)) {
      final ListLog4jAppender appender2 = (ListLog4jAppender)obj;
      return this.tableModel == appender2.tableModel;
    }
    return false;
  }

  @Override
  public void stop() {
    super.stop();
    this.tableModel.clear();
  }
}

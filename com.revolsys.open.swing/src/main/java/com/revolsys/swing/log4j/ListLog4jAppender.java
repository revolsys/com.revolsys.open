package com.revolsys.swing.log4j;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class ListLog4jAppender extends AppenderSkeleton {

  private int maxSize = 100;

  private final LinkedList<LoggingEvent> loggingEvents = new LinkedList<LoggingEvent>();

  private final Log4jTableModel tableModel;

  public ListLog4jAppender(final Log4jTableModel tableModel) {
    this.tableModel = tableModel;
  }

  @Override
  protected void append(final LoggingEvent event) {
    final int index = this.loggingEvents.size();
    this.loggingEvents.add(event);
    this.tableModel.fireTableRowsInserted(index, index);
    while (this.loggingEvents.size() > this.maxSize) {
      this.loggingEvents.removeFirst();
      this.tableModel.fireTableRowsDeleted(0, 0);
    }
  }

  @Override
  public void close() {
    this.tableModel.fireTableDataChanged();
    this.loggingEvents.clear();
  }

  public List<LoggingEvent> getLoggingEvents() {
    return this.loggingEvents;
  }

  public int getMaxSize() {
    return this.maxSize;
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  public void setMaxSize(final int maxSize) {
    this.maxSize = maxSize;
  }
}

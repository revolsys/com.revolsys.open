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
    final int index = loggingEvents.size();
    loggingEvents.add(event);
    tableModel.fireTableRowsInserted(index, index);
    while (loggingEvents.size() > maxSize) {
      loggingEvents.removeFirst();
      tableModel.fireTableRowsDeleted(0, 0);
    }
  }

  @Override
  public void close() {
    tableModel.fireTableDataChanged();
    loggingEvents.clear();
  }

  public List<LoggingEvent> getLoggingEvents() {
    return loggingEvents;
  }

  public int getMaxSize() {
    return maxSize;
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  public void setMaxSize(final int maxSize) {
    this.maxSize = maxSize;
  }
}

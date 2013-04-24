package com.revolsys.swing.log4j;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class ListLog4jAppender extends AppenderSkeleton {

  private int maxSize = 100;

  private LinkedList<LoggingEvent> loggingEvents = new LinkedList<LoggingEvent>();

  private Log4jTableModel tableModel;

  public ListLog4jAppender(Log4jTableModel tableModel) {
    this.tableModel = tableModel;
  }

  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  public int getMaxSize() {
    return maxSize;
  }

  @Override
  public void close() {
    tableModel.fireTableDataChanged();
    loggingEvents.clear();
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  @Override
  protected void append(LoggingEvent event) {
    int index = loggingEvents.size();
    loggingEvents.add(event);
    tableModel.fireTableRowsInserted(index, index);
    while (loggingEvents.size() > maxSize) {
      loggingEvents.removeFirst();
      tableModel.fireTableRowsDeleted(0, 0);
    }
  }

  public List<LoggingEvent> getLoggingEvents() {
    return loggingEvents;
  }
}

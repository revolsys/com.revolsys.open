package com.revolsys.swing.log4j;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.swing.table.BaseJxTable;

public class Log4jTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;

  private static final List<String> columnNames = Arrays.asList("Time",
    "Level", "Category", "Message");

  public static JPanel createPanel() {
    final JPanel taskPanel = new JPanel(new BorderLayout());
    final BaseJxTable table = createTable();
    final JScrollPane scrollPane = new JScrollPane(table);
    taskPanel.add(scrollPane, BorderLayout.CENTER);
    return taskPanel;
  }

  public static BaseJxTable createTable() {
    final Log4jTableModel model = new Log4jTableModel();
    final BaseJxTable table = new BaseJxTable(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumnExt column = table.getColumnExt(i);
      if (i == 0) {
        column.setMinWidth(180);
        column.setPreferredWidth(180);
        column.setMaxWidth(180);
      } else if (i == 1) {
        column.setMinWidth(80);
        column.setPreferredWidth(80);
        column.setMaxWidth(80);
      } else if (i == 2) {
        column.setMinWidth(200);
        column.setPreferredWidth(400);
      } else if (i == 3) {
        column.setMinWidth(200);
        column.setPreferredWidth(800);
      }
    }
    table.setSortOrder(0, SortOrder.DESCENDING);
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          final int row = table.rowAtPoint(e.getPoint());
          if (row != -1) {
            final LoggingEvent loggingEvent = model.getLoggingEvent(row);
            LoggingEventPanel.showDialog(table, loggingEvent);
          }
        }
      }
    });
    return table;
  }

  private final ListLog4jAppender appender = new ListLog4jAppender(this);

  public Log4jTableModel() {
    Logger.getRootLogger().addAppender(appender);
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    Logger.getRootLogger().removeAppender(appender);
  }

  @Override
  public int getColumnCount() {
    return columnNames.size();
  }

  @Override
  public String getColumnName(final int column) {
    return columnNames.get(column);
  }

  public LoggingEvent getLoggingEvent(final int rowIndex) {
    LoggingEvent event;
    final List<LoggingEvent> loggingEvents = appender.getLoggingEvents();
    if (rowIndex < loggingEvents.size()) {
      event = loggingEvents.get(rowIndex);
    } else {
      return null;
    }
    return event;
  }

  @Override
  public int getRowCount() {
    final List<LoggingEvent> loggingEvents = appender.getLoggingEvents();
    return loggingEvents.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final LoggingEvent event = getLoggingEvent(rowIndex);
    if (event == null) {
      return null;
    } else {
      switch (columnIndex) {
        case 0:
          final long time = event.getTimeStamp();
          final Timestamp timestamp = new Timestamp(time);
          return timestamp;
        case 1:
          return event.getLevel();
        case 2:
          return event.getLoggerName();
        case 3:
          return event.getMessage();
        default:
          return null;
      }
    }
  }

}

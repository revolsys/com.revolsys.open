package com.revolsys.swing.logging;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.swing.Icons;
import com.revolsys.swing.TabbedPane;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.util.Property;

public class Log4jTableModel extends AbstractTableModel {
  private static final List<String> COLUMN_NAMES = Arrays.asList("Time", "Level", "Logger Name",
    "Message");

  private static final long serialVersionUID = 1L;

  public static void addNewTabPane(final TabbedPane tabs) {
    final TablePanel panel = newPanel();

    final Log4jTableModel tableModel = panel.getTableModel();

    final int tabIndex = tabs.getTabCount();
    tabs.addTab(null, Icons.getIcon("error"), panel);

    final Log4jTabLabel tabLabel = new Log4jTabLabel(tabs, tableModel);
    tabs.setTabComponentAt(tabIndex, tabLabel);
    tabs.setSelectedIndex(tabIndex);
  }

  public static TablePanel newPanel() {
    final BaseJTable table = newTable();
    return new TablePanel(table);
  }

  public static BaseJTable newTable() {
    final Log4jTableModel model = new Log4jTableModel();
    final BaseJTable table = new BaseJTable(model);
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
          int row = table.rowAtPoint(e.getPoint());
          row = table.convertRowIndexToModel(row);
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

  private List<LoggingEvent> rows = new ArrayList<>();

  private boolean hasNewErrors = false;

  public Log4jTableModel() {
    Logger.getRootLogger().addAppender(this.appender);
    final MenuFactory menu = getMenu();
    menu.addMenuItem("all", "Delete all messages", "delete", this::clear);
    addMenuItem("selected", "Delete selected messages", "delete", (final BaseJTable table) -> {
      int count = 0;

      final int[] selectedRows = table.getSelectedRowsInModel();
      for (final int row : selectedRows) {
        final int rowIndex = row - count;
        this.rows.remove(rowIndex);
        count++;
      }
      this.hasNewErrors = false;
      fireTableDataChanged();
    });
    addMenuItem("message", "Delete message", "delete", this::removeLoggingEvent);
    addMenuItem("message", "Copy message", "page_copy", this::copyLoggingEvent);
  }

  public void addLoggingEvent(final LoggingEvent event) {
    if (event != null) {
      Invoke.later(() -> {
        if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
          this.hasNewErrors = true;
        }
        this.rows.add(event);
        while (this.rows.size() > 99) {
          this.rows.remove(0);
        }
        fireTableDataChanged();
      });
    }
  }

  public void clear() {
    Invoke.later(() -> {
      this.rows.clear();
      this.hasNewErrors = false;
      fireTableDataChanged();
    });
  }

  public void clearHasNewErrors() {
    Invoke.later(() -> {
      this.hasNewErrors = false;
      final JTable table = getTable();
      table.repaint();
    });
  }

  public void copyLoggingEvent(final int index) {
    final LoggingEvent event = getLoggingEvent(index);
    final StringBuilder plain = new StringBuilder();
    final StringBuilder html = new StringBuilder();
    final Object message = event.getMessage();
    if (message != null) {
      plain.append(message);
      html.append("<b>");
      html.append(message);
      html.append("</b>");
    }
    final String stackTrace = LoggingEventPanel.getStackTrace(event);
    if (Property.hasValue(stackTrace)) {
      if (plain.length() > 0) {
        plain.append("\n");
      }
      plain.append(stackTrace);
      html.append("<pre>");
      html.append(stackTrace);
      html.append("</pre>");
    }

    final BasicTransferable transferable = new BasicTransferable(plain.toString(), html.toString());
    ClipboardUtil.setContents(transferable);
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    Logger.getRootLogger().removeAppender(this.appender);
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.size();
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES.get(column);
  }

  public LoggingEvent getLoggingEvent(final int rowIndex) {
    try {
      if (rowIndex < this.rows.size()) {
        return this.rows.get(rowIndex);
      }
    } catch (final Throwable e) {
    }
    return null;
  }

  @Override
  public BaseJPopupMenu getMenu(final int rowIndex, final int columnIndex) {
    clearHasNewErrors();
    return super.getMenu(rowIndex, columnIndex);
  }

  public int getMessageCount() {
    return this.rows.size();
  }

  @Override
  public int getRowCount() {
    return this.rows.size();
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

  public boolean isHasMessages() {
    return !this.rows.isEmpty();
  }

  public boolean isHasNewErrors() {
    return this.hasNewErrors;
  }

  public void removeLoggingEvent(final int index) {
    Invoke.later(() -> {
      this.rows.remove(index);
      this.hasNewErrors = false;
      fireTableDataChanged();
    });
  }

  public void setRows(final List<LoggingEvent> rows) {
    Invoke.later(() -> {
      this.rows = new ArrayList<>(rows);
      fireTableDataChanged();
    });
  }
}

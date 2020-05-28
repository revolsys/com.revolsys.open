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

import org.jdesktop.swingx.plaf.basic.core.BasicTransferable;
import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.log.LogbackUtil;
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;

public class LoggingTableModel extends AbstractTableModel {
  private static final List<String> COLUMN_NAMES = Arrays.asList("Time", "Level", "Logger Name",
    "Message");

  private static final long serialVersionUID = 1L;

  public static void addNewTabPane(final TabbedPane tabs) {
    final TablePanel panel = newPanel();

    final LoggingTableModel tableModel = panel.getTableModel();

    final int tabIndex = tabs.getTabCount();
    tabs.addTab(null, Icons.getIcon("error"), panel);

    final LoggingTabLabel tabLabel = new LoggingTabLabel(tabs, tableModel);
    tabs.setTabComponentAt(tabIndex, tabLabel);
    tabs.setSelectedIndex(tabIndex);
  }

  private static BaseJTable newLogTable() {
    final LoggingTableModel model = new LoggingTableModel();
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
            final List<Object> loggingEvent = model.rows.get(row);
            LoggingEventPanel.showDialog(loggingEvent);
          }
        }
      }
    });
    return table;
  }

  public static TablePanel newPanel() {
    final BaseJTable table = newLogTable();
    return new TablePanel(table);
  }

  private final ListLoggingAppender appender = new ListLoggingAppender(this);

  private List<List<Object>> rows = new ArrayList<>();

  private boolean hasNewErrors = false;

  public LoggingTableModel() {
    LogbackUtil.addRootAppender(this.appender);
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

  public void addLoggingEvent(final ILoggingEvent event) {
    if (event != null) {
      final long time = event.getTimeStamp();
      final Timestamp timestamp = new Timestamp(time);
      final Level level = event.getLevel();
      final String loggerName = event.getLoggerName();

      final String formattedMessage = event.getFormattedMessage();
      final IThrowableProxy thrownProxy = event.getThrowableProxy();
      final List<Object> row = Arrays.asList(timestamp, level, loggerName, formattedMessage,
        event.getThreadName(), thrownProxy);
      Invoke.later(() -> {
        if (Level.ERROR.isGreaterOrEqual(event.getLevel())) {
          this.hasNewErrors = true;
        }
        this.rows.add(row);
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
    final List<Object> event = this.rows.get(index);
    final StringBuilder plain = new StringBuilder();
    final StringBuilder html = new StringBuilder();
    final Object message = event.get(3);
    if (message != null) {
      plain.append(message);
      html.append("<b>");
      html.append(message);
      html.append("</b>");
    }
    final String stackTrace = LoggingEventPanel.getStackTrace((ThrowableProxy)event.get(5));
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
    final Logger logger = LogbackUtil.getRootLogger();
    LogbackUtil.removeAppender(logger, this.appender);
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.size();
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES.get(column);
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
    if (rowIndex >= this.rows.size()) {
      return null;
    } else {
      final List<Object> values = this.rows.get(rowIndex);
      if (values == null) {
        return null;
      } else {
        return values.get(columnIndex);
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

  public void setRows(final List<List<Object>> rows) {
    Invoke.later(() -> {
      this.rows = new ArrayList<>(rows);
      fireTableDataChanged();
    });
  }
}

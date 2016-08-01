package com.revolsys.swing.table.worker;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.swing.TabbedPane;
import com.revolsys.swing.parallel.AbstractSwingWorker;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;

public class SwingWorkerTableModel extends AbstractTableModel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private static final List<String> COLUMN_TITLES = Arrays.asList("Thread", "Description",
    "Status");

  public static int addNewTabPanel(final TabbedPane tabs) {
    final JPanel panel = newPanel();
    final int tabIndex = tabs.addTabIcon("time", "Background Tasks", panel, false);
    return tabIndex;
  }

  public static JPanel newPanel() {
    final JPanel taskPanel = new JPanel(new BorderLayout());
    final BaseJTable table = SwingWorkerTableModel.newTable();
    final JScrollPane scrollPane = new JScrollPane(table);
    taskPanel.add(scrollPane, BorderLayout.CENTER);
    return taskPanel;
  }

  public static BaseJTable newTable() {
    final SwingWorkerTableModel model = new SwingWorkerTableModel();
    final BaseJTable table = new BaseJTable(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.setAutoCreateColumnsFromModel(false);

    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumnExt column = table.getColumnExt(i);
      if (i == 0) {
        column.setMinWidth(70);
        column.setPreferredWidth(70);
        column.setMaxWidth(70);
      } else if (i == 1) {
        column.setMinWidth(200);
        column.setPreferredWidth(400);
      } else if (i == 2) {
        column.setMinWidth(70);
        column.setPreferredWidth(70);
        column.setMaxWidth(70);
      }
    }
    return table;
  }

  private List<SwingWorker<?, ?>> workers = Collections.emptyList();

  public SwingWorkerTableModel() {
    final PropertyChangeSupport propertyChangeSupport = Invoke.getPropertyChangeSupport();
    propertyChangeSupport.addPropertyChangeListener(this);
  }

  @Override
  public void dispose() {
    super.dispose();
    Invoke.getPropertyChangeSupport().removePropertyChangeListener(this);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return String.class;
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return COLUMN_TITLES.get(columnIndex);
  }

  @Override
  public int getRowCount() {
    return this.workers.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (rowIndex < getRowCount()) {
      final SwingWorker<?, ?> worker = this.workers.get(rowIndex);
      if (worker != null) {
        if (columnIndex == 0) {
          if (worker.isDone()) {
            this.workers.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
          } else if (worker instanceof AbstractSwingWorker) {
            final AbstractSwingWorker<?, ?> threadWorker = (AbstractSwingWorker<?, ?>)worker;
            return threadWorker.getThreadName();
          }
        } else if (columnIndex == 1) {
          return worker.toString();
        } else {
          return worker.getState().name();
        }
      }
    }
    return "-";
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    Invoke.later(() -> {
      if (event.getPropertyName().equals("workers")) {
        this.workers = (List<SwingWorker<?, ?>>)event.getNewValue();
        fireTableDataChanged();
      }
    });
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
  }
}

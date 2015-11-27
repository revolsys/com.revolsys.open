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

import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;

public class SwingWorkerTableModel extends AbstractTableModel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private static final List<String> COLUMN_TITLES = Arrays.asList("Description", "Status");

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
        column.setMinWidth(200);
        column.setPreferredWidth(400);
      } else if (i == 1) {
        column.setMinWidth(100);
        column.setPreferredWidth(100);
        column.setMaxWidth(100);
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
    return 2;
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
    final SwingWorker<?, ?> worker = this.workers.get(rowIndex);
    if (worker == null) {
      return "-";
    } else {
      if (columnIndex == 1) {
        return worker.getState().name();
      } else {
        return worker.toString();
      }
    }
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

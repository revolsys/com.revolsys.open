package com.revolsys.swing.table.object;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.swing.table.SortableTableCellHeaderRenderer;

public class ObjectListTable extends JTable {
  private static final long serialVersionUID = 1L;

  public ObjectListTable(final ObjectListTableModel model) {
    super(model);
    setModel(model);
    final ObjectListTableCellRenderer cellRenderer = new ObjectListTableCellRenderer();

    final TableCellRenderer headerRenderer = new SortableTableCellHeaderRenderer();
    final JTableHeader tableHeader = getTableHeader();
    tableHeader.setReorderingAllowed(false);
    tableHeader.setDefaultRenderer(headerRenderer);

    final TableColumnModel columnModel = getColumnModel();
    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);

      column.setCellRenderer(cellRenderer);
    }
    model.addTableModelListener(this);
  }

  @Override
  public ObjectListTableModel getModel() {
    return (ObjectListTableModel)super.getModel();
  }

  @Override
  public void tableChanged(TableModelEvent e) {
    super.tableChanged(e);
    if (tableHeader != null) {
      tableHeader.resizeAndRepaint();
    }
  }

  public <T> T getSelectedObject() {
    int selectedRow = getSelectedRow();
    if (selectedRow > -1) {
      ObjectListTableModel model = getModel();
      return (T)model.getObject(selectedRow);
    } else {
      return null;
    }
  }
}

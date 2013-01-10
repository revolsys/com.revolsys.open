package com.revolsys.swing.table.object;

import java.util.List;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.swing.table.SortableTableCellHeaderRenderer;

public class ObjectListTable extends JXTable {
  private static final long serialVersionUID = 1L;

  public ObjectListTable(final ObjectListTableModel model) {
    super(model);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setModel(model);
    final ObjectListTableCellRenderer cellRenderer = new ObjectListTableCellRenderer();

    final TableCellRenderer headerRenderer = new SortableTableCellHeaderRenderer();
    final JTableHeader tableHeader = getTableHeader();
    tableHeader.setReorderingAllowed(false);
    tableHeader.setDefaultRenderer(headerRenderer);

    for (int i = 0; i < model.getColumnCount(); i++) {
      TableColumnExt column = getColumnExt(i);
      column.sizeWidthToFit();
      column.setCellRenderer(cellRenderer);
    }
    model.addTableModelListener(this);
  }

  public ObjectListTable(String... columnNames) {
    this(new ObjectListTableModel(columnNames));
  }

  public ObjectListTable(List<String> columnNames, List<String> columnTitles) {
    this(new ObjectListTableModel(columnNames, columnTitles));
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

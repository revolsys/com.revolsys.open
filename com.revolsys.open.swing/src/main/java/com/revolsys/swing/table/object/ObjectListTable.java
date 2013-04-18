package com.revolsys.swing.table.object;

import java.util.List;

import javax.swing.event.TableModelEvent;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.swing.table.BaseJxTable;

public class ObjectListTable extends BaseJxTable {
  private static final long serialVersionUID = 1L;

  public ObjectListTable(final List<String> columnNames,
    final List<String> columnTitles) {
    this(new ObjectListTableModel(columnNames, columnTitles));
  }

  public ObjectListTable(final ObjectListTableModel model) {
    super(model);

    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumnExt column = getColumnExt(i);
      column.sizeWidthToFit();
    }
    model.addTableModelListener(this);
  }

  public ObjectListTable(final String... columnNames) {
    this(new ObjectListTableModel(columnNames));
  }

  public ObjectListTableModel getObjectListTableModel() {
    return (ObjectListTableModel)super.getModel();
  }

  public <T> T getSelectedObject() {
    final int selectedRow = getSelectedRow();
    if (selectedRow > -1) {
      final ObjectListTableModel model = getObjectListTableModel();
      return (T)model.getObject(selectedRow);
    } else {
      return null;
    }
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    super.tableChanged(e);
    if (tableHeader != null) {
      tableHeader.resizeAndRepaint();
    }
  }
}

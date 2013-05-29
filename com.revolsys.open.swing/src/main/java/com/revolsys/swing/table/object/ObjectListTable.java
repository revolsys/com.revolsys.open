package com.revolsys.swing.table.object;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.TableModelEvent;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.swing.table.BaseJxTable;

public class ObjectListTable<T> extends BaseJxTable implements Iterable<T> {
  private static final long serialVersionUID = 1L;

  public ObjectListTable(final List<String> columnNames,
    final List<String> columnTitles) {
    this(new ObjectListTableModel<T>(columnNames, columnTitles));
  }

  public ObjectListTable(final ObjectListTableModel<T> model) {
    super(model);

    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumnExt column = getColumnExt(i);
      column.sizeWidthToFit();
    }
    model.addTableModelListener(this);
  }

  public ObjectListTable(final String... columnNames) {
    this(new ObjectListTableModel<T>(columnNames));
  }

  @SuppressWarnings("unchecked")
  public ObjectListTableModel<T> getObjectListTableModel() {
    return (ObjectListTableModel<T>)super.getModel();
  }

  @SuppressWarnings("unchecked")
  public <V> V getSelectedObject() {
    final int selectedRow = getSelectedRow();
    if (selectedRow > -1) {
      final ObjectListTableModel<T> model = getObjectListTableModel();
      return (V)model.getObject(selectedRow);
    } else {
      return null;
    }
  }

  @Override
  public Iterator<T> iterator() {
    return getObjects().iterator();
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    super.tableChanged(e);
    if (tableHeader != null) {
      tableHeader.resizeAndRepaint();
    }
  }

  public void setObjects(Collection<? extends T> objects) {
    getObjectListTableModel().setObjects(objects);
  }

  public List<T> getObjects() {
    return getObjectListTableModel().getObjects();
  }
}

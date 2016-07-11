package com.revolsys.swing.table.object;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.TableModelEvent;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.swing.table.BaseJTable;

public class ObjectListTable<T> extends BaseJTable implements Iterable<T> {
  private static final long serialVersionUID = 1L;

  public ObjectListTable(final List<String> columnNames, final List<String> columnTitles) {
    this(new ObjectListTableModel<T>(columnNames, columnTitles));
  }

  public ObjectListTable(final List<T> objects, final List<String> columnNames,
    final List<String> titles) {
    this(new ObjectListTableModel<>(objects, columnNames, titles));
  }

  public ObjectListTable(final List<T> objects, final List<String> columnNames,
    final List<String> columnTitles, final List<Class<?>> columnClasses) {
    this(new ObjectListTableModel<>(objects, columnNames, columnTitles, columnClasses));
  }

  public ObjectListTable(final ObjectListTableModel<T> model) {
    super(model);

    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumnExt column = getColumnExt(i);
      column.sizeWidthToFit();
    }
  }

  public ObjectListTable(final String... columnNames) {
    this(new ObjectListTableModel<T>(columnNames));
  }

  @SuppressWarnings("unchecked")
  public ObjectListTableModel<T> getObjectListTableModel() {
    return (ObjectListTableModel<T>)super.getModel();
  }

  public List<T> getObjects() {
    return getObjectListTableModel().getObjects();
  }

  @SuppressWarnings("unchecked")
  public <V> V getSelectedObject() {
    final int selectedRow = getSelectedRowInModel();
    if (selectedRow > -1) {
      final ObjectListTableModel<T> model = getObjectListTableModel();
      return (V)model.getObject(selectedRow);
    } else {
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public ObjectListTableModel<T> getTableModel() {
    return (ObjectListTableModel<T>)getModel();
  }

  @Override
  public Iterator<T> iterator() {
    return getObjects().iterator();
  }

  @Override
  public void removeNotify() {
    getTableModel().dispose();
    super.removeNotify();
  }

  public void setObjects(final Collection<? extends T> objects) {
    getObjectListTableModel().setObjects(objects);
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    try {
      super.tableChanged(e);
      if (this.tableHeader != null) {
        this.tableHeader.resizeAndRepaint();
      }
    } catch (final Throwable t) {
    }
  }
}

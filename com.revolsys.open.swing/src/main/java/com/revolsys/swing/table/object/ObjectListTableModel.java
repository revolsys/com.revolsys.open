package com.revolsys.swing.table.object;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.swing.table.AbstractTableModel;

import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Reorderable;

public class ObjectListTableModel extends AbstractTableModel implements
  Reorderable {

  private static final long serialVersionUID = 1L;

  private final List<String> columnNames = new ArrayList<String>();

  private final List<String> columnTitles = new ArrayList<String>();

  private boolean editable;

  private final List<Object> objects = new ArrayList<Object>();

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public ObjectListTableModel(final Collection<? extends Object> objects,
    final List<String> columnNames) {
    this(objects, columnNames, columnNames);
  }

  public ObjectListTableModel(final Collection<? extends Object> objects,
    final List<String> columnNames, final List<String> columnTitles) {
    this.objects.addAll(objects);
    this.columnNames.addAll(columnNames);
    this.columnTitles.addAll(columnTitles);
    setEditable(true);
  }

  public ObjectListTableModel(final List<String> columnNames,
    final List<String> columnTiList) {
    this(Collections.emptyList(), columnNames, columnTiList);
  }

  public ObjectListTableModel(final String... columnNames) {
    this(Collections.emptyList(), Arrays.asList(columnNames),
      Arrays.asList(columnNames));
    setEditable(false);
  }

  public void add(final int index, final Object object) {
    objects.add(index, object);
    fireTableRowsInserted(index, index);
  }

  public void add(final Object... objects) {
    if (objects.length > 0) {
      final int startIndex = this.objects.size();
      for (final Object object : objects) {
        this.objects.add(object);
      }
      final int endIndex = this.objects.size() - 1;
      fireTableRowsInserted(startIndex, endIndex);
    }
  }

  public void addAll(final Collection<Object> objects) {
    if (objects.size() > 0) {
      final int startIndex = this.objects.size();
      this.objects.addAll(objects);
      final int endIndex = this.objects.size() - 1;
      fireTableRowsInserted(startIndex, endIndex);
    }
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  public void clear() {
    objects.clear();
    fireTableDataChanged();
  }

  @PreDestroy
  public void dispose() {
    objects.clear();
  }

  private void firePropertyChange(final Object object, final String name,
    final Object oldValue, final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  public String getAttributeName(final int columnIndex) {
    return columnNames.get(columnIndex);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return Object.class;
  }

  @Override
  public int getColumnCount() {
    return columnNames.size();
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return columnTitles.get(columnIndex);
  }

  public Object getObject(final int index) {
    if (index < objects.size()) {
      return objects.get(index);
    } else {
      return null;
    }
  }

  /**
   * @return the objects
   */
  public List<Object> getObjects() {
    return objects;
  }

  public List<Object> getObjects(final int[] rows) {
    final List<Object> objects = new ArrayList<Object>();
    for (final int row : rows) {
      final Object object = getObject(row);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(propertyChangeListeners);
  }

  @Override
  public int getRowCount() {
    return objects.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Object object = getObject(rowIndex);
    if (object == null) {
      return null;
    } else {
      final String name = getAttributeName(columnIndex);
      return JavaBeanUtil.getValue(object, name);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return isEditable();
  }

  public boolean isEditable() {
    return editable;
  }

  public void remove(final int... rows) {
    final List<Object> rowsToRemove = getObjects(rows);
    removeAll(rowsToRemove);
  }

  public void removeAll(final Collection<Object> objects) {
    for (final Object object : objects) {
      final int row = this.objects.indexOf(object);
      if (row != -1) {
        this.objects.remove(row);
        fireTableRowsDeleted(row, row + 1);
      }
    }
  }

  public void removeAll(final Object... removedFeatures) {
    removeAll(Arrays.asList(removedFeatures));
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final Object object = getObject(fromIndex);
    removeAll(object);
    add(toIndex, object);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  /**
   * @param objects the objects to set
   */
  public void setObjects(final List<? extends Object> objects) {
    this.objects.clear();
    if (objects != null) {
      this.objects.addAll(objects);
    }
    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final Object object = getObject(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = JavaBeanUtil.getValue(object, name);
      JavaBeanUtil.setProperty(object, name, value);
      firePropertyChange(object, name, oldValue, value);
    }
  }
}

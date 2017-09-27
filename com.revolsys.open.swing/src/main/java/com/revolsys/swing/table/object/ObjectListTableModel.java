package com.revolsys.swing.table.object;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.collection.PropertyChangeArrayList;
import com.revolsys.collection.list.Lists;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;
import com.revolsys.util.Reorderable;

public class ObjectListTableModel<T> extends AbstractTableModel
  implements Reorderable, PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private final List<String> columnNames = new ArrayList<>();

  private final List<String> columnTitles = new ArrayList<>();

  private List<Class<?>> columnClasses;

  private boolean editable;

  private PropertyChangeArrayList<T> objects;

  public ObjectListTableModel(final Collection<? extends T> objects,
    final List<String> columnNames) {
    this(objects, columnNames, columnNames);
  }

  public ObjectListTableModel(final Collection<? extends T> objects, final List<String> columnNames,
    final List<String> columnTitles) {
    this(objects, columnNames, columnTitles, Collections.emptyList());
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public ObjectListTableModel(final Collection<? extends T> objects, final List<String> columnNames,
    final List<String> columnTitles, final List<Class<?>> columnClasses) {
    if (objects == null) {
      this.objects = new PropertyChangeArrayList<>();
    } else if (objects instanceof PropertyChangeArrayList) {
      this.objects = (PropertyChangeArrayList)objects;
    } else {
      this.objects = new PropertyChangeArrayList<>(objects);
    }
    Property.addListener(this.objects, this);
    this.columnNames.addAll(columnNames);
    for (int i = 0; i < columnNames.size(); i++) {
      final String columnName = columnNames.get(i);
      String columnTitle;
      if (columnTitles == null || i >= columnTitles.size()) {
        columnTitle = CaseConverter.toCapitalizedWords(columnName);
      } else {
        columnTitle = columnTitles.get(i);
      }
      this.columnTitles.add(columnTitle);
    }
    this.columnClasses = Lists.toArray(columnClasses);
    setEditable(true);
  }

  public ObjectListTableModel(final List<String> columnNames, final List<String> columnTitles) {
    this(Collections.<T> emptyList(), columnNames, columnTitles);
  }

  public ObjectListTableModel(final String... columnNames) {
    this(Collections.<T> emptyList(), Arrays.asList(columnNames), null);
    setEditable(false);
  }

  public void add(final int index, final T object) {
    this.objects.add(index, object);
  }

  @SuppressWarnings("unchecked")
  public void add(final T... objects) {
    addAll(Arrays.asList(objects));
  }

  public void addAll(final Collection<? extends T> objects) {
    this.objects.addAll(objects);
  }

  public void clear() {
    this.objects.clear();
  }

  @Override
  @PreDestroy
  public void dispose() {
    Property.removeListener(this.objects, this);
    this.objects = null;
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex >= 0 && columnIndex < this.columnClasses.size()) {
      return this.columnClasses.get(columnIndex);
    } else {
      return Object.class;
    }
  }

  @Override
  public int getColumnCount() {
    return this.columnNames.size();
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return this.columnTitles.get(columnIndex);
  }

  public String getFieldName(final int columnIndex) {
    return this.columnNames.get(columnIndex);
  }

  public T getObject(final int index) {
    if (this.objects != null) {
      if (index < this.objects.size()) {
        return this.objects.get(index);
      }
    }
    return null;
  }

  /**
   * @return the objects
   */
  public List<T> getObjects() {
    return this.objects;
  }

  public List<Object> getObjects(final int[] rows) {
    final List<Object> objects = new ArrayList<>();
    for (final int row : rows) {
      final Object object = getObject(row);
      if (object != null) {
        objects.add(object);
      }
    }
    return objects;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.objects.getPropertyChangeSupport();
  }

  @Override
  public int getRowCount() {
    return this.objects.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final Object object = getObject(rowIndex);
    if (object == null) {
      return null;
    } else {
      try {
        final String name = getFieldName(columnIndex);
        if (name.equals("#")) {
          return rowIndex + 1;
        } else {
          return Property.get(object, name);
        }
      } catch (final Throwable t) {
        return null;
      }
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    final String name = getFieldName(columnIndex);
    if (name.equals("#")) {
      return false;
    } else {
      return isEditable();
    }
  }

  public boolean isEditable() {
    return this.editable;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getSource() == this.objects) {
      if (event instanceof IndexedPropertyChangeEvent) {
        final IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent)event;
        final int index = indexedEvent.getIndex();
        if (indexedEvent.getNewValue() == null) {
          fireTableRowsDeleted(index, index);
        } else if (indexedEvent.getOldValue() == null) {
          fireTableRowsInserted(index, index);
        } else {
          fireTableRowsUpdated(index, index);
        }
      } else {
        fireTableDataChanged();
      }
    }
  }

  public void remove(final int... rows) {
    final List<Object> rowsToRemove = getObjects(rows);
    removeAll(rowsToRemove);
  }

  public void removeAll(final Collection<Object> objects) {
    this.objects.removeAll(objects);
  }

  public void removeAll(final Object... removedFeatures) {
    removeAll(Arrays.asList(removedFeatures));
  }

  public void removeObject(final Object object) {
    this.objects.remove(object);
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final T object = getObject(fromIndex);
    removeAll(object);
    add(toIndex, object);
  }

  public void setEditable(final boolean editable) {
    this.editable = editable;
  }

  /**
   * @param objects the objects to set
   */
  public void setObjects(final Collection<? extends T> objects) {
    this.objects.clear();
    if (objects != null) {
      this.objects.addAll(objects);
    }
    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final Object object = getObject(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = Property.get(object, name);
      Property.setSimple(object, name, value);
      firePropertyChange(object, name, oldValue, value);
    }
  }
}

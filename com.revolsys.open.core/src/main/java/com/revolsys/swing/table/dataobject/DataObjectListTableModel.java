package com.revolsys.swing.table.dataobject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.table.Reorderable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListTableModel extends AbstractTableModel implements
  Reorderable {
  private static int compareBoolean(final Boolean b1, final Boolean b2) {
    final boolean bool1 = b1.booleanValue();
    final boolean bool2 = b2.booleanValue();
    if (bool1 == bool2) {
      return 0;
    }
    return bool1 ? 1 : -1;
  }

  private static int compareValue(final Object o1, final Object o2) {
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }

    if (o1 instanceof Boolean) {
      return compareBoolean((Boolean)o1, (Boolean)o2);
    } else if (o1 instanceof Geometry) {
      return 0; // for now - change to compare type
    } else if (o1 instanceof Comparable) {
      final Comparable attribute1 = (Comparable)o1;
      final Comparable attribute2 = (Comparable)o2;
      return attribute1.compareTo(attribute2);
    }
    return 0;
  }

  private List<String> columnNames = new ArrayList<String>();

  private final List<DataObject> objects = new ArrayList<DataObject>();

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  private DataObjectMetaData metaData;

  private boolean sortAscending = true;

  private int sortedColumnIndex = -1;

  private String sortedColumnName = null;

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    this(metaData, metaData.getAttributeNames(), objects);
  }

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    List<String> columnNames, final List<DataObject> objects) {
    this.metaData = metaData;
    this.objects.addAll(objects);
    this.columnNames = new ArrayList<String>(columnNames);
  }

  public void addAll(final Collection<DataObject> objects) {
    this.objects.clear();
    this.objects.addAll(objects);
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  public void dispose() {
    metaData = null;
    objects.clear();
  }

  private void firePropertyChange(final DataObject object, final String name,
    final Object oldValue, final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  @Override
  public int getColumnCount() {
    return columnNames.size();
  }

  @Override
  public String getColumnName(final int column) {
    return columnNames.get(column);
  }

  public DataObject getObject(final int index) {
    return objects.get(index);
  }

  public List<DataObject> getObjects(final int[] rows) {
    final List<DataObject> objects = new ArrayList<DataObject>();
    for (final int row : rows) {
      final DataObject object = getObject(row);
      objects.add(object);
    }
    return objects;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  /**
   * @return the objects
   */
  public List<DataObject> getObjects() {
    return objects;
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(propertyChangeListeners);
  }

  @Override
  public int getRowCount() {
    return objects.size();
  }

  /**
   * @return the sortedColumnIndex
   */
  public int getSortedColumnIndex() {
    return sortedColumnIndex;
  }

  public String getSortedColumnName() {
    return sortedColumnName;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final DataObject object = getObject(rowIndex);
    if (object == null) {
      return null;
    } else {
      String columnName = getColumnName(columnIndex);
      return object.getValue(columnName);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    String columnName = getColumnName(columnIndex);
    DataType dataType = metaData.getAttributeType(columnName);
    if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * @return the sortAscending
   */
  public boolean isSortAscending() {
    return sortAscending;
  }

  public void removeAll(DataObject... removedFeatures) {
    removeAll(Arrays.asList(removedFeatures));
  }

  public void removeAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      final int row = this.objects.indexOf(object);
      if (row != -1) {
        this.objects.remove(row);
        fireTableRowsDeleted(row, row + 1);
      }
    }
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  /**
   * @param objects the objects to set
   */
  public void setObjects(final List<DataObject> objects) {
    this.objects.clear();
    if (objects != null) {
      this.objects.addAll(objects);
    }
    fireTableDataChanged();
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final DataObject object = getObject(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = object.getValueByPath(name);
      object.setValue(name, value);
      firePropertyChange(object, name, oldValue, value);
    }
  }

  public void sort(final int index) {
    if (index < getColumnCount()) {
      if (index == sortedColumnIndex) {
        sortAscending = !sortAscending;
      } else {
        sortAscending = true;
      }
      sortedColumnIndex = index;
      final String attributeName = getColumnName(index);
      final Comparator<DataObject> comparitor = new DataObjectAttributeComparator(
        sortAscending, attributeName);
      Collections.sort(objects, comparitor);
      fireTableDataChanged();
    }
  }

  public void sort(final String columnName) {
    if (columnName != null) {
      sort(columnName, columnName.equals(sortedColumnName) ? (!sortAscending)
        : true);
    }
  }

  public void sort(final String columnName, final boolean ascending) {
    this.sortAscending = ascending;
    this.sortedColumnName = columnName;

    final int column = metaData.getAttributeIndex(columnName);
    Collections.sort(objects, new Comparator<Object>() {
      private int ascendingCompare(final Object o1, final Object o2) {
        final DataObject f1 = (DataObject)o1;
        final DataObject f2 = (DataObject)o2;

        final Object v1 = f1.getValue(column);
        final Object v2 = f2.getValue(column);
        return compareValue(v1, v2);
      }

      @Override
      public int compare(final Object o1, final Object o2) {
        return ascendingCompare(o1, o2) * (ascending ? 1 : (-1));
      }
    });
  }

  public void add(DataObject... objects) {
    for (DataObject object : objects) {
      this.objects.add(object);
      fireTableRowsInserted(this.objects.size() - 1, this.objects.size());
    }
  }

  public void remove(int... rows) {
    List<DataObject> rowsToRemove = getObjects(rows);
    removeAll(rowsToRemove);
  }

  @Override
  public void reorder(int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    DataObject object = getObject(fromIndex);
    removeAll(object);
    add(toIndex, object);
  }

  public void add(int index, DataObject object) {
    objects.add(index, object);
    fireTableRowsInserted(index, index + 1);
  }

}

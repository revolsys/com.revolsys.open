package com.revolsys.swing.table.dataobject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListTableModel extends AbstractTableModel {
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

  private final List<DataObject> objects = new ArrayList<DataObject>();

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  private DataObjectMetaData metaData;

  private boolean sortAscending = true;

  private int sortedColumnIndex = -1;

  private String sortedColumnName = null;

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    this.metaData = metaData;
    this.objects.addAll(objects);
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

  private void firePropertyChange(
    final DataObject feature,
    final String name,
    final Object oldValue,
    final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(feature, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  @Override
  public int getColumnCount() {
    return metaData.getAttributeCount();
  }

  @Override
  public String getColumnName(final int column) {
    return metaData.getAttributeName(column);
  }

  public DataObject getFeature(final int index) {
    return objects.get(index);
  }

  /**
   * @return the objects
   */
  public List<DataObject> getObjects() {
    return objects;
  }

  public List<DataObject> getFeatures(final int[] rows) {
    final List<DataObject> features = new ArrayList<DataObject>();
    for (final int row : rows) {
      final DataObject feature = getFeature(row);
      features.add(feature);
    }
    return features;
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(propertyChangeListeners);
  }

  @Override
  public int getRowCount() {
    return objects.size();
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
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
    final DataObject feature = getFeature(rowIndex);
    if (feature == null) {
      return null;
    } else {
      return feature.getValue(columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (Geometry.class.isAssignableFrom(metaData.getAttributeType(columnIndex)
      .getJavaClass())) {
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

  public void remove(final Collection<DataObject> removedFeatures) {
    if (this.objects.removeAll(removedFeatures)) {
      fireTableDataChanged();
    }
  }

  public void removeAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      final int row = this.objects.indexOf(object);
      if (row != -1) {
        objects.remove(row);
        TableModelEvent event = new TableModelEvent(this, row, row,
          TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        fireTableChanged(event);
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
  public void setValueAt(
    final Object value,
    final int rowIndex,
    final int columnIndex) {
    final DataObject feature = getFeature(rowIndex);
    if (feature != null) {
      final Object oldValue = feature.getValue(columnIndex);
      final String name = metaData.getAttributeName(columnIndex);
      feature.setValue(columnIndex, value);
      firePropertyChange(feature, name, oldValue, value);
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
      String attributeName = metaData.getAttributeName(index);
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

}

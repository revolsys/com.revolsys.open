package com.revolsys.swing.table.dataobject.row;

import java.awt.BorderLayout;
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

import javax.annotation.PreDestroy;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.util.Reorderable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListTableModel extends DataObjectRowTableModel implements
  Reorderable {
  private static final long serialVersionUID = 1L;

  public static JPanel createPanel(DataObjectMetaData metaData,
    final List<DataObject> objects) {
    JTable table = createTable(metaData, objects);
    final JScrollPane scrollPane = new JScrollPane(table);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  public static DataObjectRowTable createTable(
    final DataObjectMetaData metaData, final List<DataObject> objects) {
    final DataObjectListTableModel model = new DataObjectListTableModel(
      metaData, objects);
    return new DataObjectRowTable(model);
  }

  private final List<DataObject> objects = new ArrayList<DataObject>();

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    this(metaData, metaData.getAttributeNames(), objects);
  }

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    List<String> columnNames, final List<DataObject> objects) {
    super(metaData, columnNames);
    this.objects.addAll(objects);
    setEditable(true);
  }

  public void addAll(final Collection<DataObject> objects) {
    this.objects.clear();
    this.objects.addAll(objects);
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  @PreDestroy
  public void dispose() {
    super.dispose();
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

  public DataObject getObject(final int index) {
    return objects.get(index);
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

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      String columnName = getColumnName(columnIndex);
      DataObjectMetaData metaData = getMetaData();
      DataType dataType = metaData.getAttributeType(columnName);
      if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
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

  @Override
  public SortOrder setSortOrder(int column) {
    SortOrder sortOrder = super.setSortOrder(column);
    final String attributeName = getAttributeName(column);
    final Comparator<DataObject> comparitor = new DataObjectAttributeComparator(
      sortOrder == SortOrder.ASCENDING, attributeName);
    Collections.sort(objects, comparitor);
    fireTableDataChanged();
    return sortOrder;
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
    clearSortedColumns();
  }

  public void add(int index, DataObject object) {
    objects.add(index, object);
    fireTableRowsInserted(index, index + 1);
  }

}

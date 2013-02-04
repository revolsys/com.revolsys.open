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

  public static JPanel createPanel(final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    final JTable table = createTable(metaData, objects);
    final JScrollPane scrollPane = new JScrollPane(table);
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  public static DataObjectRowJxTable createTable(
    final DataObjectMetaData metaData, final List<DataObject> objects) {
    final DataObjectListTableModel model = new DataObjectListTableModel(
      metaData, objects);
    return new DataObjectRowJxTable(model);
  }

  private final List<DataObject> objects = new ArrayList<DataObject>();

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    this(metaData, metaData.getAttributeNames(), objects);
  }

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    final List<String> columnNames, final List<DataObject> objects) {
    super(metaData, columnNames);
    this.objects.addAll(objects);
    setEditable(true);
  }

  public void add(final DataObject... objects) {
    for (final DataObject object : objects) {
      this.objects.add(object);
      fireTableRowsInserted(this.objects.size() - 1, this.objects.size());
    }
  }

  public void add(final int index, final DataObject object) {
    objects.add(index, object);
    fireTableRowsInserted(index, index + 1);
  }

  public void addAll(final Collection<DataObject> objects) {
    this.objects.clear();
    this.objects.addAll(objects);
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  @Override
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

  @Override
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
      final String columnName = getColumnName(columnIndex);
      final DataObjectMetaData metaData = getMetaData();
      final DataType dataType = metaData.getAttributeType(columnName);
      if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public void remove(final int... rows) {
    final List<DataObject> rowsToRemove = getObjects(rows);
    removeAll(rowsToRemove);
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

  public void removeAll(final DataObject... removedFeatures) {
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
    final DataObject object = getObject(fromIndex);
    removeAll(object);
    add(toIndex, object);
    clearSortedColumns();
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
  public SortOrder setSortOrder(final int column) {
    final SortOrder sortOrder = super.setSortOrder(column);
    final String attributeName = getAttributeName(column);
    final Comparator<DataObject> comparitor = new DataObjectAttributeComparator(
      sortOrder == SortOrder.ASCENDING, attributeName);
    Collections.sort(objects, comparitor);
    fireTableDataChanged();
    return sortOrder;
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

}

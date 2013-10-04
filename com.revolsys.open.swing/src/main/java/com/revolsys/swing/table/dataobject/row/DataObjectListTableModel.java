package com.revolsys.swing.table.dataobject.row;

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
import javax.swing.JTable;
import javax.swing.SortOrder;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.util.Reorderable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListTableModel extends DataObjectRowTableModel implements
  Reorderable {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final DataObjectLayer layer) {
    return createPanel(layer.getMetaData(), new ArrayList<LayerDataObject>(),
      layer.getColumnNames());
  }

  public static TablePanel createPanel(final DataObjectLayer layer,
    final Collection<LayerDataObject> objects) {
    return createPanel(layer.getMetaData(), objects, layer.getColumnNames());
  }

  public static TablePanel createPanel(final DataObjectMetaData metaData,
    final Collection<LayerDataObject> objects,
    final Collection<String> attributeNames) {
    final DataObjectListTableModel model = new DataObjectListTableModel(
      metaData, objects, attributeNames);
    final JTable table = new DataObjectRowTable(model);
    return new TablePanel(table);
  }

  public static TablePanel createPanel(final DataObjectMetaData metaData,
    final List<LayerDataObject> objects, final String... attributeNames) {
    return createPanel(metaData, objects, Arrays.asList(attributeNames));
  }

  private final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public DataObjectListTableModel(final DataObjectMetaData metaData,
    final Collection<LayerDataObject> objects,
    final Collection<String> columnNames) {
    super(metaData, columnNames);
    if (objects != null) {
      this.objects.addAll(objects);
    }
    setEditable(true);
  }

  public void add(final int index, final LayerDataObject object) {
    this.objects.add(index, object);
    fireTableRowsInserted(index, index + 1);
  }

  public void add(final LayerDataObject... objects) {
    for (final LayerDataObject object : objects) {
      this.objects.add(object);
      fireTableRowsInserted(this.objects.size() - 1, this.objects.size());
    }
  }

  public void addAll(final Collection<LayerDataObject> objects) {
    this.objects.clear();
    this.objects.addAll(objects);
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  public void clear() {
    this.objects.clear();
    fireTableDataChanged();
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.objects.clear();
  }

  private void firePropertyChange(final DataObject object, final String name,
    final Object oldValue, final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : this.propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  @Override
  public LayerDataObject getObject(final int index) {
    return this.objects.get(index);
  }

  /**
   * @return the objects
   */
  public List<LayerDataObject> getObjects() {
    return this.objects;
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(this.propertyChangeListeners);
  }

  @Override
  public int getRowCount() {
    return this.objects.size();
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final String columnName = getColumnName(columnIndex);
      final DataObjectMetaData metaData = getMetaData();
      final DataType dataType = metaData.getAttributeType(columnName);
      if (dataType == null) {
        return false;
      } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public void remove(final int... rows) {
    final List<LayerDataObject> rowsToRemove = getObjects(rows);
    removeAll(rowsToRemove);
  }

  public void removeAll(final Collection<LayerDataObject> objects) {
    for (final LayerDataObject object : objects) {
      final int row = this.objects.indexOf(object);
      if (row != -1) {
        this.objects.remove(row);
        fireTableRowsDeleted(row, row + 1);
      }
    }
  }

  public void removeAll(final LayerDataObject... removedFeatures) {
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
    final LayerDataObject object = getObject(fromIndex);
    removeAll(object);
    add(toIndex, object);
    clearSortedColumns();
  }

  /**
   * @param objects the objects to set
   */
  public void setObjects(final List<LayerDataObject> objects) {
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
    Collections.sort(this.objects, comparitor);
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

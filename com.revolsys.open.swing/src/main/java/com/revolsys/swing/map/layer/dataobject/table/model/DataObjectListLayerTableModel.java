package com.revolsys.swing.map.layer.dataobject.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.dataobject.DataObjectListLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListLayerTableModel extends DataObjectLayerTableModel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static DataObjectLayerTable createTable(final DataObjectListLayer layer) {
    final DataObjectLayerTableModel model = new DataObjectListLayerTableModel(
      layer);
    final DataObjectLayerTable table = new DataObjectLayerTable(model);

    layer.addPropertyChangeListener("hasSelectedRecords",
      new InvokeMethodPropertyChangeListener(DataObjectLayerTableModel.class,
        "selectionChanged", table, model));

    return table;
  }

  private DataObjectListLayer layer;

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public DataObjectListLayerTableModel(final DataObjectListLayer layer) {
    this(layer, layer.getMetaData().getAttributeNames());
  }

  public DataObjectListLayerTableModel(final DataObjectListLayer layer,
    final List<String> columnNames) {
    super(layer, columnNames);
    this.layer = layer;
    layer.addPropertyChangeListener("records", this);
    setEditable(false);
    setSortableModes(MODE_SELECTED, MODE_ALL);
  }

  @Override
  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    this.layer = null;
  }

  private void firePropertyChange(final DataObject object, final String name,
    final Object oldValue, final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : this.propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends DataObject> V getObject(final int row) {
    final String attributeFilterMode = getAttributeFilterMode();
    if (attributeFilterMode.equals(MODE_SELECTED)) {
      final List<LayerDataObject> selectedObjects = getSelectedObjects();
      if (row < selectedObjects.size()) {
        return (V)selectedObjects.get(row);
      } else {
        return null;
      }
    } else {
      return (V)this.layer.getRecord(row);
    }
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(this.propertyChangeListeners);
  }

  @Override
  public int getRowCountInternal() {
    final String attributeFilterMode = getAttributeFilterMode();
    if (attributeFilterMode.equals(MODE_SELECTED)) {
      return this.layer.getSelectionCount();
    } else {
      return this.layer.getRowCount();
    }
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

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    super.propertyChange(evt);
  }

  @Override
  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  @Override
  public boolean setFilter(final Condition filter) {
    if (super.setFilter(filter)) {
      setRowSorter(filter);
      return true;
    } else {
      return false;
    }
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

  /*
   * TODO @Override public SortOrder setSortOrder(int column) { SortOrder
   * sortOrder = super.setSortOrder(column); final String attributeName =
   * getAttributeName(column); final Comparator<DataObject> comparitor = new
   * ObjectPropertyComparator( sortOrder == SortOrder.ASCENDING, attributeName);
   * Collections.sort(objects, comparitor); fireTableDataChanged(); return
   * sortOrder; }
   */

}

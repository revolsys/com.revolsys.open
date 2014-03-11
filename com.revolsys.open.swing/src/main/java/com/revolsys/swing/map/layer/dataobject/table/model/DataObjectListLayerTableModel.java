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
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.swing.map.layer.dataobject.DataObjectListLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.table.DataObjectLayerTable;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListLayerTableModel extends DataObjectLayerTableModel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static DataObjectLayerTable createTable(final DataObjectListLayer layer) {
    final DataObjectLayerTableModel model = new DataObjectListLayerTableModel(
      layer);
    final DataObjectLayerTable table = new DataObjectLayerTable(model);

    Property.addListener(layer, "hasSelectedRecords", new InvokeMethodListener(
      DataObjectLayerTableModel.class, "selectionChanged", table, model));

    return table;
  }

  private DataObjectListLayer layer;

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  private List<LayerDataObject> records = Collections.emptyList();

  public DataObjectListLayerTableModel(final DataObjectListLayer layer) {
    this(layer, layer.getMetaData().getAttributeNames());
  }

  public DataObjectListLayerTableModel(final DataObjectListLayer layer,
    final List<String> columnNames) {
    super(layer, columnNames);
    this.layer = layer;
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

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(this.propertyChangeListeners);
  }

  @Override
  public int getRowCountInternal() {
    if (getAttributeFilterMode().equals(MODE_ALL)) {
      final Query query = getFilterQuery();
      query.setOrderBy(getOrderBy());
      this.records = layer.query(query);
      return this.records.size();
    } else {
      return super.getRowCountInternal();
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
  protected LayerDataObject loadLayerRecord(final int row) {
    if (row >= 0 && row < this.records.size()) {
      return records.get(row);
    } else {
      return null;
    }
  }

  @Override
  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final DataObject record = getRecord(rowIndex);
    if (record != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = record.getValueByPath(name);
      record.setValue(name, value);
      firePropertyChange(record, name, oldValue, value);
    }
  }
}

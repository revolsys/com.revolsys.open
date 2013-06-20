package com.revolsys.swing.map.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.dataobject.AbstractDataObjectTableModel;

@SuppressWarnings("serial")
public class DataObjectLayerAttributesTableModel extends
  AbstractDataObjectTableModel implements PropertyChangeListener {

  public static JComponent create(final LayerDataObject object,
    final boolean editable) {
    final DataObjectLayerAttributesTableModel model = new DataObjectLayerAttributesTableModel(
      object, editable);
    return create(model);
  }

  private LayerDataObject object;

  private final DataObjectLayer layer;

  public DataObjectLayerAttributesTableModel(final DataObjectLayer layer,
    final boolean editable) {
    super(layer.getMetaData(), editable);
    this.layer = layer;
    layer.addPropertyChangeListener(this);
  }

  public DataObjectLayerAttributesTableModel(final LayerDataObject object,
    final boolean editable) {
    this(object.getLayer(), editable);
    this.object = object;
  }

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public String getColumnName(final int column) {
    if (column == 3) {
      return "Original Value";
    } else {
      return super.getColumnName(column);
    }
  }

  public LayerDataObject getObject() {
    return object;
  }

  @Override
  protected Object getValue(final int rowIndex) {
    if (object == null) {
      return "-";
    } else {
      return object.getValue(rowIndex);
    }
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (object == null) {
      return null;
    } else if (columnIndex == 3) {
      final String attributeName = getAttributeName(rowIndex);
      return object.getOriginalValue(attributeName);
    } else {
      return super.getValueAt(rowIndex, columnIndex);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == object) {
      final String propertyName = event.getPropertyName();
      final DataObjectMetaData metaData = getMetaData();
      final int index = metaData.getAttributeIndex(propertyName);
      if (index > -1) {
        try {
          fireTableRowsUpdated(index, index);
        } catch (final Throwable t) {
        }
      }
    }
  }

  public void removeListener() {
    layer.removePropertyChangeListener(this);
  }

  public void setObject(final LayerDataObject object) {
    this.object = object;
  }

  @Override
  protected Object setValue(final Object value, final int rowIndex) {
    final Object oldValue = object.getValue(rowIndex);
    object.setValue(rowIndex, value);
    return oldValue;
  }
}

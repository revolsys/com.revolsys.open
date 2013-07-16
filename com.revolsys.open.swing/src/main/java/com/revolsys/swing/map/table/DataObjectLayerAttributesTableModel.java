package com.revolsys.swing.map.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.dataobject.AbstractDataObjectTableModel;

@SuppressWarnings("serial")
public class DataObjectLayerAttributesTableModel extends
  AbstractDataObjectTableModel implements PropertyChangeListener {

  private LayerDataObject object;

  private final DataObjectLayer layer;

  private final DataObjectLayerForm form;

  public DataObjectLayerAttributesTableModel(final DataObjectLayerForm form) {
    super(form.getMetaData(), true);
    this.form = form;
    this.layer = form.getLayer();
    this.object = form.getObject();
    layer.addPropertyChangeListener(this);
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
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      if (form.isEditable()) {
        final String idAttributeName = getMetaData().getIdAttributeName();
        final String attributeName = getAttributeName(rowIndex);
        if (attributeName.equals(idAttributeName)) {
          return false;
        } else {
          return form.isEditable(attributeName);
        }
      } else {
        return false;
      }
    } else {
      return false;
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

package com.revolsys.swing.map.layer.dataobject.table.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.dataobject.model.AbstractSingleDataObjectTableModel;

public class DataObjectLayerAttributesTableModel extends
  AbstractSingleDataObjectTableModel implements PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private LayerDataObject object;

  private final DataObjectLayer layer;

  private final DataObjectLayerForm form;

  public DataObjectLayerAttributesTableModel(final DataObjectLayerForm form) {
    super(form.getMetaData(), true);
    this.form = form;
    this.layer = form.getLayer();
    this.object = form.getObject();
    this.layer.addPropertyChangeListener(this);
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
    return this.object;
  }

  @Override
  public Object getObjectValue(final int rowIndex) {
    if (this.object == null) {
      return null;
    } else {
      return this.object.getValue(rowIndex);
    }
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (this.object == null) {
      return null;
    } else if (columnIndex == 3) {
      final String attributeName = getAttributeName(rowIndex);
      return this.object.getOriginalValue(attributeName);
    } else {
      return super.getValueAt(rowIndex, columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      if (this.form.isEditable()) {
        final String idAttributeName = getMetaData().getIdAttributeName();
        final String attributeName = getAttributeName(rowIndex);
        if (attributeName.equals(idAttributeName)) {
          return false;
        } else {
          return this.form.isEditable(attributeName);
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
    if (source == this.object) {
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
    this.layer.removePropertyChangeListener(this);
  }

  public void setObject(final LayerDataObject object) {
    this.object = object;
  }

  @Override
  protected Object setObjectValue(final int rowIndex, final Object value) {
    final Object oldValue = this.object.getValue(rowIndex);
    this.object.setValue(rowIndex, value);
    return oldValue;
  }
}

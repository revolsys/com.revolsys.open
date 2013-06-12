package com.revolsys.swing.map.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

@SuppressWarnings("serial")
public class DataObjectLayerForm extends DataObjectForm implements
  PropertyChangeListener {
  private final DataObjectLayer layer;

  public DataObjectLayerForm(final DataObjectLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
    final DataObjectMetaData metaData = layer.getMetaData();
    addTabAllAttributes();
    final boolean editable = layer.isEditable();
    setEditable(editable);
    getAllAttributes().setEditable(editable);
    if (metaData.getGeometryAttributeName() != null) {
      addTabGeometry();
    }
    layer.addPropertyChangeListener(this);
    // TODO mark the object as attribute editing
  }

  public DataObjectLayerForm(final DataObjectLayer layer,
    final DataObject object) {
    this(layer);
    setObject(object);
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  @Override
  public LayerDataObject getObject() {
    return (LayerDataObject)super.getObject();
  }

  @Override
  public <T> T getOriginalValue(final String fieldName) {
    final LayerDataObject object = getObject();
    return object.getOriginalValue(fieldName);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    if (event.getSource() == getObject()) {
      final String propertyName = event.getPropertyName();
      final Object value = event.getNewValue();
      final DataObjectMetaData metaData = getMetaData();
      if (metaData.hasAttribute(propertyName)) {
        setFieldValue(propertyName, value);
      }
    }
  }

  @Override
  public void removeNotify() {
    try {
      super.removeNotify();
    } finally {
      layer.removePropertyChangeListener(this);
    }
  }
}

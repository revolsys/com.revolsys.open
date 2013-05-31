package com.revolsys.swing.map.form;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

@SuppressWarnings("serial")
public class DataObjectLayerForm extends DataObjectForm {
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
}

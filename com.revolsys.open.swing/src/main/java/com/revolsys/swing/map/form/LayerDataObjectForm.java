package com.revolsys.swing.map.form;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

@SuppressWarnings("serial")
public class LayerDataObjectForm extends DataObjectForm {
  private final DataObjectLayer layer;

  public LayerDataObjectForm(final DataObjectLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
    DataObjectMetaData metaData = layer.getMetaData();
    addTabAllAttributes();
    getAllAttributes().setEditable(layer.isEditable());
    if (metaData.getGeometryAttributeName() != null) {
      addTabGeometry();
    }
  }

  public DataObjectLayer getLayer() {
    return layer;
  }
}

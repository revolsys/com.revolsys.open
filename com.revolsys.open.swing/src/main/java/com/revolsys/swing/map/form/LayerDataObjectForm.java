package com.revolsys.swing.map.form;

import java.awt.BorderLayout;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

@SuppressWarnings("serial")
public class LayerDataObjectForm extends DataObjectForm {
  private final DataObjectLayer layer;

  public LayerDataObjectForm(final DataObjectLayer layer, DataObject object) {
    this.layer = layer;
    DataObjectMetaData metaData = layer.getMetaData();
    setMetaData(metaData);
    add(addTabAllAttributes(), BorderLayout.CENTER);
    getAllAttributes().setEditable(layer.isEditable());
    if (metaData.getGeometryAttributeName() != null) {
      addTabGeometry();
    }
    setValues(object);
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  
}

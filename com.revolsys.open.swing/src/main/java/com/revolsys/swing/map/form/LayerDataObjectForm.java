package com.revolsys.swing.map.form;

import java.awt.BorderLayout;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

@SuppressWarnings("serial")
public class LayerDataObjectForm extends DataObjectForm {
  private final DataObjectLayer layer;

  public LayerDataObjectForm(final DataObjectLayer layer, DataObject object) {
    this.layer = layer;
    setMetaData(layer.getMetaData());
    setValues(object);
    add(createAllAttributesPanel(), BorderLayout.CENTER);
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

}

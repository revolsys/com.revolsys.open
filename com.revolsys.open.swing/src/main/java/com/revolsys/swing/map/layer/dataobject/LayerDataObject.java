package com.revolsys.swing.map.layer.dataobject;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.swing.map.layer.Layer;

public class LayerDataObject extends ArrayDataObject {

  private Layer layer;

  public LayerDataObject(DataObjectLayer layer) {
    super(layer.getMetaData());
    this.layer = layer;
  }

  public Layer getLayer() {
    return layer;
  }
}

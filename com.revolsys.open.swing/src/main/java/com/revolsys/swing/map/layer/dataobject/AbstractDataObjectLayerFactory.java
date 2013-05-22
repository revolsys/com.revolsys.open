package com.revolsys.swing.map.layer.dataobject;

import com.revolsys.swing.map.layer.AbstractLayerFactory;

public abstract class AbstractDataObjectLayerFactory<T extends DataObjectLayer>
  extends AbstractLayerFactory<T> {

  public AbstractDataObjectLayerFactory(String typeName, String description) {
    super(typeName, description);
  }

}

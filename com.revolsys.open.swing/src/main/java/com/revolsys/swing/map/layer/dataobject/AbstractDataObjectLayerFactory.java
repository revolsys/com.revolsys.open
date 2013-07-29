package com.revolsys.swing.map.layer.dataobject;

import com.revolsys.io.map.AbstractMapObjectFactory;

public abstract class AbstractDataObjectLayerFactory extends
  AbstractMapObjectFactory {

  public AbstractDataObjectLayerFactory(final String typeName,
    final String description) {
    super(typeName, description);
  }

}

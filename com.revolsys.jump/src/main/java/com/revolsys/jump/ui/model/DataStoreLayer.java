package com.revolsys.jump.ui.model;

import com.revolsys.gis.data.io.DataObjectStore;
import com.vividsolutions.jump.workbench.model.Layer;

public class DataStoreLayer extends Layer {
  private DataObjectStore dataStore;

  public DataStoreLayer(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

}

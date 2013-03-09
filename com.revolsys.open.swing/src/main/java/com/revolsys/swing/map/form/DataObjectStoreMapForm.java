package com.revolsys.swing.map.form;

import java.awt.LayoutManager;

import com.revolsys.gis.data.io.DataObjectStore;

public class DataObjectStoreMapForm extends DataObjectForm {
  private static final long serialVersionUID = 1L;

  public DataObjectStoreMapForm() {
  }

  public DataObjectStoreMapForm(LayoutManager layout) {
    super(layout);
  }

  private DataObjectStore dataStore;

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  public void setDataStore(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }
}

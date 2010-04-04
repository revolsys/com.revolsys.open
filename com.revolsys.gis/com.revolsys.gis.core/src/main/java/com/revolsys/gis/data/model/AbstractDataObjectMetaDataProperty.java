package com.revolsys.gis.data.model;

public abstract class AbstractDataObjectMetaDataProperty implements
  DataObjectMetaDataProperty {
  private DataObjectMetaData metaData;

  @Override
  public abstract DataObjectMetaDataProperty clone();

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public void setMetaData(
    final DataObjectMetaData metaData) {
    if (this.metaData != null) {
      this.metaData.setProperty(getPropertyName(), null);
    }
    this.metaData = metaData;
    if (metaData != null) {
      metaData.setProperty(getPropertyName(), this);
    }
  }
}

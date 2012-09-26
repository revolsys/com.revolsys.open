package com.revolsys.gis.data.model;

public interface DataObjectMetaDataProperty extends Cloneable {

  DataObjectMetaDataProperty cloneCoordinates();

  DataObjectMetaData getMetaData();

  String getPropertyName();

  void setMetaData(DataObjectMetaData metaData);
}

package com.revolsys.gis.data.model;


public interface DataObjectMetaDataProperty extends Cloneable {

  DataObjectMetaDataProperty clone();

  DataObjectMetaData getMetaData();

  String getPropertyName();

  void setMetaData(
    DataObjectMetaData metaData);
}

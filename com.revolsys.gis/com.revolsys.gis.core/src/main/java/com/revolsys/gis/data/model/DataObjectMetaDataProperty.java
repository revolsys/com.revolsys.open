package com.revolsys.gis.data.model;


public interface DataObjectMetaDataProperty {

  DataObjectMetaDataProperty clone();

  DataObjectMetaData getMetaData();

  String getPropertyName();

  void setMetaData(
    DataObjectMetaData metaData);
}

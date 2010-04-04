package com.revolsys.gis.data.model;

import javax.xml.namespace.QName;

public interface DataObjectMetaDataProperty {

  DataObjectMetaDataProperty clone();

  DataObjectMetaData getMetaData();

  QName getPropertyName();

  void setMetaData(
    DataObjectMetaData metaData);
}

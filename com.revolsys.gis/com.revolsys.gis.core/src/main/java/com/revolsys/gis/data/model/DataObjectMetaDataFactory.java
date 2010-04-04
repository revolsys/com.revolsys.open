package com.revolsys.gis.data.model;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.ObjectWithProperties;

public interface DataObjectMetaDataFactory extends ObjectWithProperties{
  DataObjectMetaData getMetaData(
    QName typeName);

}

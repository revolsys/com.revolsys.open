package com.revolsys.gis.data.model;

import javax.xml.namespace.QName;

import com.revolsys.io.ObjectWithProperties;

public interface DataObjectMetaDataFactory extends ObjectWithProperties{
  DataObjectMetaData getMetaData(
    QName typeName);

}

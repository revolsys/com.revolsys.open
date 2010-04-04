package com.revolsys.gis.data.model.types;

import javax.xml.namespace.QName;

public interface DataType {

  Class<?> getJavaClass();

  QName getName();

}

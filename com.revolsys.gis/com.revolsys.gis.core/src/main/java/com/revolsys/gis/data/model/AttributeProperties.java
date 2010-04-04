package com.revolsys.gis.data.model;

import javax.xml.namespace.QName;

public interface AttributeProperties {
  QName SRID = new QName("http://gis.revolsys.com/", "srid");
  QName COORDINATE_SYSTEM = new QName("http://gis.revolsys.com/", "coordinateSystem");
}

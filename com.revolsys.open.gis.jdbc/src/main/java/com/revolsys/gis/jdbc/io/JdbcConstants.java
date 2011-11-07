package com.revolsys.gis.jdbc.io;

import javax.xml.namespace.QName;

public interface JdbcConstants {
  QName FUNCTION_INTERSECTS = new QName(
    "http://gis.revolsys.com/jdbc/function", "intersects");

  QName FUNCTION_BUFFER = new QName("http://gis.revolsys.com/jdbc/function",
    "buffer");
}

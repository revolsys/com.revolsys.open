package com.revolsys.gis.data.query;

public interface Condition {

  void appendSql(StringBuffer buffer);
}

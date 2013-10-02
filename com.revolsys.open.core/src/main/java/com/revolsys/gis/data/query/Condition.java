package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.List;

public interface Condition extends Cloneable {
  // TODO wrap in a more generic structure
  int appendParameters(int index, PreparedStatement statement);

  void appendSql(StringBuffer buffer);

  Condition clone();

  List<Condition> getConditions();

  boolean isEmpty();

  String toFormattedString();
}

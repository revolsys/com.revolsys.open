package com.revolsys.gis.data.model.codes;

import java.util.List;
import java.util.Map;

public interface CodeTable extends Cloneable {
  CodeTable clone();

  Map<Number, List<Object>> getCodes();

  Number getId(
    final Map<String, ? extends Object> values);

  Number getId(
    final Object... values);

  String getIdColumn();

  Map<String, ? extends Object> getMap(
    final Number id);

  <V> V getValue(
    final Number id);

  List<Object> getValues(
    final Number id);

}

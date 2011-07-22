package com.revolsys.gis.data.model.codes;

import java.util.List;
import java.util.Map;

public interface CodeTable<I> extends Cloneable {
  CodeTable<I> clone();

  List<String> getAttributeAliases();

  List<String> getValueAttributeNames();

  Map<I, List<Object>> getCodes();

  I getId(final Map<String, ? extends Object> values);

  I getId(final Object... values);

  String getIdAttributeName();

  Map<String, ? extends Object> getMap(final I id);

  <V> V getValue(final I id);

  List<Object> getValues(final I id);
}

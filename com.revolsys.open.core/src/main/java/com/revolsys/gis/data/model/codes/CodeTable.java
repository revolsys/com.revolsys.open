package com.revolsys.gis.data.model.codes;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

public interface CodeTable extends Cloneable {
  List<String> getAttributeAliases();

  Map<Object, List<Object>> getCodes();

  <T> T getId(final Map<String, ? extends Object> values);

  <T> T getId(final Object... values);

  String getIdAttributeName();

  Map<String, ? extends Object> getMap(final Object id);

  String getName();

  JComponent getSwingEditor();

  <V> V getValue(final Object id);

  List<String> getValueAttributeNames();

  List<Object> getValues(final Object id);

  void refresh();
}

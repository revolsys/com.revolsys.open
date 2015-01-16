package com.revolsys.data.codes;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.data.identifier.Identifier;

public interface CodeTable extends Cloneable {
  Map<Identifier, List<Object>> getCodes();

  List<String> getFieldAliases();

  Identifier getId(final Map<String, ? extends Object> values);

  Identifier getId(final Object... values);

  Identifier getIdExact(final Object... values);

  String getIdFieldName();

  Map<String, ? extends Object> getMap(final Identifier id);

  String getName();

  JComponent getSwingEditor();

  <V> V getValue(final Identifier id);

  <V> V getValue(final Object id);

  List<String> getValueFieldNames();

  List<Object> getValues(final Identifier id);

  void refresh();
}

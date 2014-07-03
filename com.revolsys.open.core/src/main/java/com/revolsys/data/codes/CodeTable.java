package com.revolsys.data.codes;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.data.identifier.Identifier;

public interface CodeTable extends Cloneable {
  List<String> getAttributeAliases();

  Map<Identifier, List<Object>> getCodes();

  Identifier getId(final Map<String, ? extends Object> values);

  Identifier getId(final Object... values);

  String getIdAttributeName();

  Map<String, ? extends Object> getMap(final Identifier id);

  String getName();

  JComponent getSwingEditor();

  <V> V getValue(final Object id);

  <V> V getValue(final Identifier id);

  List<String> getValueAttributeNames();

  List<Object> getValues(final Identifier id);

  void refresh();
}

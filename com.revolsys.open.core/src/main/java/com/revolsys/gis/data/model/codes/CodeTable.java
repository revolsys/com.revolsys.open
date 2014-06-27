package com.revolsys.gis.data.model.codes;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.revolsys.gis.data.model.RecordIdentifier;

public interface CodeTable extends Cloneable {
  List<String> getAttributeAliases();

  Map<RecordIdentifier, List<Object>> getCodes();

  RecordIdentifier getId(final Map<String, ? extends Object> values);

  RecordIdentifier getId(final Object... values);

  String getIdAttributeName();

  Map<String, ? extends Object> getMap(final RecordIdentifier id);

  String getName();

  JComponent getSwingEditor();

  <V> V getValue(final Object id);

  <V> V getValue(final RecordIdentifier id);

  List<String> getValueAttributeNames();

  List<Object> getValues(final RecordIdentifier id);

  void refresh();
}

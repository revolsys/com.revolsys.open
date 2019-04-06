package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;

import org.jeometry.common.datatype.DataType;

import com.revolsys.beans.Classes;
import com.revolsys.collection.list.Lists;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.util.CompareUtil;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;

public interface CodeTable extends Emptyable, Cloneable, Comparator<Object> {
  @Override
  default int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (value2 == null) {
      return -1;
    } else {
      final Object codeValue1 = getValue(Identifier.newIdentifier(value1));
      final Object codeValue2 = getValue(Identifier.newIdentifier(value2));
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  Map<Identifier, List<Object>> getCodes();

  default List<String> getFieldNameAliases() {
    return Collections.emptyList();
  }

  default Identifier getIdentifier(final List<Object> values) {
    return getIdentifier(values, true);
  }

  default Identifier getIdentifier(final List<Object> values, final boolean loadMissing) {
    for (final Entry<Identifier, List<Object>> entry : getCodes().entrySet()) {
      if (DataType.equal(entry.getValue(), values)) {
        return entry.getKey();
      }
    }
    return null;
  }

  default Identifier getIdentifier(final Map<String, ? extends Object> valueMap) {
    final List<String> valueFieldNames = getValueFieldNames();
    final List<Object> values = new ArrayList<>();
    for (final String name : valueFieldNames) {
      final Object value = valueMap.get(name);
      values.add(value);
    }
    return getIdentifier(values);
  }

  default Identifier getIdentifier(final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    return getIdentifier(valueList);
  }

  default List<Identifier> getIdentifiers() {
    final Map<Identifier, List<Object>> codes = getCodes();
    final Set<Identifier> keySet = codes.keySet();
    return Lists.toArray(keySet);
  }

  default Identifier getIdExact(final List<Object> values) {
    return getIdExact(values, true);
  }

  default Identifier getIdExact(final List<Object> values, final boolean loadValues) {
    return getIdentifier(values);
  }

  default Identifier getIdExact(final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    return getIdExact(valueList);
  }

  String getIdFieldName();

  default <V> V getIdValue(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Identifier identifier = getIdentifier(value);
      if (identifier == null) {
        return null;
      } else {
        return identifier.getValue(0);
      }
    }
  }

  default Map<String, ? extends Object> getMap(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<>();
      for (int i = 0; i < values.size(); i++) {
        final String name = getValueFieldNames().get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  default Map<String, ? extends Object> getMap(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getMap(identifier);
  }

  default String getName() {
    return Classes.className(getClass());
  }

  default JComponent getSwingEditor() {
    return null;
  }

  @SuppressWarnings("unchecked")
  default <V> V getValue(final Identifier id) {
    final List<Object> values = getValues(id);
    if (Property.hasValue(values)) {
      return (V)values.get(0);
    } else {
      return null;
    }
  }

  default <V> V getValue(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getValue(identifier);
  }

  default FieldDefinition getValueFieldDefinition() {
    return null;
  }

  default List<String> getValueFieldNames() {
    return Arrays.asList("VALUE");
  }

  default <V> List<V> getValues() {
    final List<V> values = new ArrayList<>();
    for (final Identifier identifier : getIdentifiers()) {
      final V value = getValue(identifier);
      values.add(value);
    }
    return values;
  }

  default List<Object> getValues(final Identifier id) {
    if (id != null) {
      final Map<Identifier, List<Object>> codes = getCodes();
      final List<Object> values = codes.get(id);
      if (values != null) {
        return Collections.unmodifiableList(values);
      }
    }
    return null;
  }

  default List<Object> getValues(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getValues(identifier);
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

  default boolean isLoadAll() {
    return true;
  }

  default boolean isLoaded() {
    return true;
  }

  default boolean isLoading() {
    return false;
  }

  default void refresh() {
  }

  default void refreshIfNeeded() {
    synchronized (this) {
      if (!isLoaded() && !isLoading()) {
        refresh();
      }
    }
  }
}

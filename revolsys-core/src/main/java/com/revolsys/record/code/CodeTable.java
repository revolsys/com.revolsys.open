package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JComponent;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.refresh.Refreshable;

import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.util.Emptyable;

public interface CodeTable
  extends Emptyable, Cloneable, Comparator<Object>, BaseCloseable, Refreshable {
  static CodeTable newCodeTable(final Map<String, ? extends Object> config) {
    if (config.containsKey("valueFieldNames")) {
      return new MultiValueCodeTableProperty(config);
    } else {
      return new SingleValueCodeTableProperty(config);
    }
  }

  static CodeTable newCodeTable(final String name, final Object source) {
    try (
      final RecordReader reader = RecordReader.newRecordReader(source)) {
      final int fieldCount = reader.getRecordDefinition().getFieldCount();
      if (fieldCount == 2) {
        return SingleValueCodeTable.newCodeTable(name, reader);
      } else {
        return MultiValueCodeTable.newCodeTable(name, reader);
      }
    }
  }

  default void addValue(final Record record) {
    throw new UnsupportedOperationException("addValue");
  }

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

  default List<String> getFieldNameAliases() {
    return Collections.emptyList();
  }

  Identifier getIdentifier(final List<Object> values);

  default Identifier getIdentifier(final Map<String, ? extends Object> valueMap) {
    final List<String> valueFieldNames = getValueFieldNames();
    final List<Object> values = new ArrayList<>();
    for (final String name : valueFieldNames) {
      final Object value = valueMap.get(name);
      values.add(value);
    }
    return getIdentifier(values);
  }

  Identifier getIdentifier(final Object value);

  default Identifier getIdentifier(final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    return getIdentifier(valueList);
  }

  List<Identifier> getIdentifiers();

  default Identifier getIdExact(final List<Object> values) {
    return getIdentifier(values);
  }

  default Identifier getIdExact(final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    return getIdExact(valueList);
  }

  default Identifier getIdExact(final Object value) {
    return getIdExact(Collections.singletonList(value));
  }

  String getIdFieldName();

  default JsonObject getMap(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return JsonObject.EMPTY;
    } else {
      final JsonObject map = JsonObject.hash();
      int i = 0;
      for (final String name : getValueFieldNames()) {
        final Object value = values.get(i);
        map.addValue(name, value);
        i++;
      }
      return map;
    }
  }

  default Map<String, ? extends Object> getMap(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getMap(identifier);
  }

  default String getName() {
    return getClass().getSimpleName();
  }

  default JComponent getSwingEditor() {
    return null;
  }

  <V> V getValue(final Identifier id);

  default <V> V getValue(final Identifier id, final Consumer<V> action) {
    return getValue(id);
  }

  default <V> V getValue(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getValue(identifier);
  }

  default <V> V getValue(final Object id, final Consumer<V> action) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getValue(identifier, action);
  }

  default FieldDefinition getValueFieldDefinition() {
    return null;
  }

  int getValueFieldLength();

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

  List<Object> getValues(final Identifier id);

  default List<Object> getValues(final Identifier id, final Consumer<List<Object>> action) {
    return getValues(id);
  }

  default List<Object> getValues(final Object id) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getValues(identifier);
  }

  default List<Object> getValues(final Object id, final Consumer<List<Object>> action) {
    final Identifier identifier = Identifier.newIdentifier(id);
    return getValues(identifier, action);
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

  @Override
  default void refresh() {
  }

  @Override
  default void refreshIfNeeded() {
    synchronized (this) {
      if (!isLoaded() && !isLoading()) {
        refresh();
      }
    }
  }

  default CodeTable setLoadMissingCodes(final boolean loadMissingCodes) {
    throw new UnsupportedOperationException("setLoadMissingNodes");
  }
}

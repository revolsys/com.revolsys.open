package com.revolsys.record.code;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class SingleValueCodeTable extends AbstractCodeTable {

  static CodeTable newCodeTable(final String name, final RecordReader reader) {
    final SingleValueCodeTable codeTable = new SingleValueCodeTable(name);
    for (final Record record : reader) {
      final Object id = record.getValue(0);
      final Object value = record.getValue(1);
      codeTable.addValue(id, value);
    }
    return codeTable;
  }

  public SingleValueCodeTable(final String name) {
    setName(name);
  }

  public SingleValueCodeTable addValue(final Object id, final Object value) {
    final Identifier identifier = Identifier.newIdentifier(id);
    setValueToId(identifier, value);
    setValueToId(identifier, getNormalizedValue(value));
    return this;
  }

  @Override
  public SingleValueCodeTable clone() {
    return (SingleValueCodeTable)super.clone();
  }

  protected Identifier getIdByValue(Object value) {
    Identifier identifier = super.getValueFromId(value);
    if (identifier != null) {
      return identifier;
    }
    identifier = super.getIdentifier(value);
    if (identifier != null) {
      return identifier;
    }
    final Object normalizedValue = getNormalizedValue(value);
    identifier = super.getValueFromId(normalizedValue);
    return identifier;
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      return getIdentifier(value);
    } else {
      return null;
    }
  }

  @Override
  public Identifier getIdentifier(final Object... values) {
    if (values != null && values.length == 1) {
      final Object value = values[0];
      return getIdentifier(value);
    } else {
      return null;
    }
  }

  @Override
  public Identifier getIdExact(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      return getIdExact(value);
    }
    return null;
  }

  @Override
  public Identifier getIdExact(final Object... values) {
    if (values != null && values.length == 1) {
      final Object value = values[0];
      return super.getIdExact(value);
    } else {
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Identifier id) {
    return (V)getValueById(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Identifier id, Consumer<V> action) {
    return (V)getValueById(id);
  }

  public final Object getValueById(Object id) {
    if (id == null) {
      return null;
    }
    Object value = super.getValueFromId(id);

    if (value == null) {
      if (super.hasValue(id)) {
        if (id instanceof SingleIdentifier) {
          final SingleIdentifier identifier = (SingleIdentifier)id;
          return identifier.getValue(0);
        } else {
          return id;
        }
      } else {
        final Identifier identifier = super.getIdentifier(id);
        if (identifier != null) {
          value = super.getValueFromId(id);
        }
      }
    }
    return value;
  }

  @Override
  public final List<Object> getValues(final Identifier id) {
    final Object value = getValue(id);
    if (value == null) {
      return null;
    } else {
      return Collections.singletonList(value);
    }
  }

  @Override
  public final List<Object> getValues(final Identifier id, Consumer<List<Object>> action) {
    final Object value = getValue(id, v -> action.accept(Collections.singletonList(v)));
    if (value == null) {
      return null;
    } else {
      return Collections.singletonList(value);
    }
  }

  public void setValues(final MapEx values) {
    for (final String key : values.keySet()) {
      final Object value = values.get(key);
      addValue(key, value);
    }
  }

}

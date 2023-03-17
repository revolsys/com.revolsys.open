package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class MultiValueCodeTable extends AbstractCodeTable {

  static CodeTable newCodeTable(final String name, final RecordReader reader) {
    final MultiValueCodeTable codeTable = new MultiValueCodeTable(name);
    final int fieldCount = reader.getRecordDefinition().getFieldCount();
    for (final Record record : reader) {
      final Identifier id = record.getIdentifier(0);
      final List<Object> values = new ArrayList<>();
      for (int i = 1; i < fieldCount; i++) {
        final Object value = record.getValue(i);
        values.add(value);
      }
      codeTable.addValue(id, values);
    }
    return codeTable;
  }

  public MultiValueCodeTable(final String name) {
    setName(name);
  }

  protected void addValue(final Identifier id, final List<Object> values) {
    super.addIdentifierAndValue(id, values);
    setValueToId(id, values);
    setValueToId(id, getNormalizedValues(values));
  }

  public void addValue(final Object id, final Object... values) {
    final Identifier identifier = Identifier.newIdentifier(id);
    final List<Object> valueList = Arrays.asList(values);
    addValue(identifier, valueList);
  }

  @Override
  public MultiValueCodeTable clone() {
    return (MultiValueCodeTable)super.clone();
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    if (values.size() == 1) {
      final Object id = values.get(0);
      final Identifier identifier = super.getIdentifier(id);
      if (identifier != null) {
        return identifier;
      }
    }

    Identifier id = super.getValueFromId(values);
    if (id == null) {
      final List<Object> normalizedValues = getNormalizedValues(values);
      id = super.getValueFromId(normalizedValues);
    }
    return id;
  }

  @Override
  public Identifier getIdExact(final List<Object> values) {
    return super.getIdExact((Object)values);
  }

  private List<Object> getNormalizedValues(final List<Object> values) {
    final List<Object> normalizedValues = new ArrayList<>();
    for (final Object value : values) {
      final Object normalizedValue = getNormalizedValue(value);
      normalizedValues.add(normalizedValue);
    }
    return normalizedValues;
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    if (id != null) {
      if (hasValue(Collections.singletonList(id))) {
        if (id instanceof SingleIdentifier) {
          final SingleIdentifier identifier = (SingleIdentifier)id;
          return Collections.singletonList(identifier.getValue(0));
        } else {
          return Collections.singletonList(id);
        }
      } else {
        List<Object> values = super.getValueFromId(id);
        if (values == null) {
          final Identifier identifier = super.getIdentifier(id);
          if (identifier != null) {
            values = super.getValueFromId(identifier);
          }
        }
        return Collections.unmodifiableList(values);
      }
    }
    return null;
  }

}

package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.util.Property;

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
    addEntry(id, values);
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
    return getIdentifier((Object)values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Identifier id) {
    final List<Object> values = getValues(id);
    if (Property.hasValue(values)) {
      return (V)values.get(0);
    } else {
      return null;
    }
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    final CodeTableEntry entry = getEntry(null, id);
    return CodeTableEntry.getValue(entry);
  }

  @Override
  public boolean isMultiValue() {
    return true;
  }

}

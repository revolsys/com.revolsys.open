package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.Resource;

public class SimpleCodeTable extends AbstractCodeTable {

  public static CodeTable newCodeTable(final String name, final Resource resource) {
    final SimpleCodeTable codeTable = new SimpleCodeTable(name);
    try (
      final RecordReader reader = RecordReader.newRecordReader(resource)) {
      for (final Record record : reader) {
        final Identifier id = Identifier.newIdentifier(record.getValue(0));
        final List<Object> values = new ArrayList<>();
        final int fieldCount = record.getRecordDefinition().getFieldCount();
        for (int i = 1; i < fieldCount; i++) {
          final Object value = record.getValue(i);
          values.add(value);
        }
        codeTable.addValue(id, values);
      }
    }
    return codeTable;
  }

  public SimpleCodeTable(final String name) {
    setName(name);
  }

  @Override
  public void addValue(final Identifier id, final Object... values) {
    super.addValue(id, values);
  }

  public void addValue(final Object id, final Object... values) {
    super.addValue(Identifier.newIdentifier(id), values);
  }

  @Override
  public SimpleCodeTable clone() {
    return (SimpleCodeTable)super.clone();
  }

  @Override
  public String getIdFieldName() {
    return getName();
  }

  @Override
  protected Identifier loadId(final List<Object> values, final boolean createId) {
    return null;
  }

  @Override
  public void refresh() {
  }

}

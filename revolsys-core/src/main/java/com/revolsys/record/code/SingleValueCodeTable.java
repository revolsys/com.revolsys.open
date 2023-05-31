package com.revolsys.record.code;

import org.jeometry.common.data.identifier.Identifier;

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
    addEntry(identifier, value);
    return this;
  }

  @Override
  public SingleValueCodeTable clone() {
    return (SingleValueCodeTable)super.clone();
  }

  public void setValues(final MapEx values) {
    for (final String key : values.keySet()) {
      final Object value = values.get(key);
      addValue(key, value);
    }
  }

}

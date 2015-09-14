package com.revolsys.record;

import java.util.Map.Entry;

import com.revolsys.record.schema.RecordDefinition;

public class RecordMapEntry implements Entry<String, Object> {

  private final int index;

  private final Record record;

  public RecordMapEntry(final Record record, final int index) {
    this.record = record;
    this.index = index;
  }

  @Override
  public String getKey() {
    final RecordDefinition recordDefinition = this.record.getRecordDefinition();
    return recordDefinition.getFieldName(this.index);
  }

  @Override
  public Object getValue() {
    return this.record.getValue(this.index);
  }

  @Override
  public Object setValue(final Object value) {
    final Object oldValue = getValue();
    this.record.setValue(this.index, value);
    return oldValue;
  }
}

package com.revolsys.swing.map.layer.record;

import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.RecordDefinition;

public class OriginalRecord implements Record {
  private final LayerRecord record;

  public OriginalRecord(final LayerRecord record) {
    this.record = record;
  }

  @Override
  public Record clone() {
    return this.record;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.record.getRecordDefinition();
  }

  @Override
  public <T> T getValue(final int index) {
    final String fieldName = getFieldName(index);
    return this.record.getOriginalValue(fieldName);
  }

  @Override
  public void setState(final RecordState state) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    throw new UnsupportedOperationException();
  }
}

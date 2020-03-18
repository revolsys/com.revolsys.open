package com.revolsys.swing.map.layer.record;

import com.revolsys.record.AbstractRecord;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class OriginalRecord extends AbstractRecord {
  private final LayerRecord record;

  public OriginalRecord(final LayerRecord record) {
    this.record = record;
  }

  @Override
  public LayerRecord clone() {
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
  public boolean isState(final RecordState state) {
    return this.record.isState(state);
  }

  @Override
  public RecordState setState(final RecordState state) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected boolean setValue(final FieldDefinition fieldDefinition, final Object value) {
    throw new UnsupportedOperationException();
  }
}

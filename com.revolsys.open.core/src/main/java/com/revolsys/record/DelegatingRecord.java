package com.revolsys.record;

import com.revolsys.record.schema.RecordDefinition;

public class DelegatingRecord extends AbstractRecord {
  private final Record record;

  public DelegatingRecord(final Record record) {
    this.record = record;
  }

  @Override
  public RecordFactory getFactory() {
    return this.record.getFactory();
  }

  public Record getRecord() {
    return this.record;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.record.getRecordDefinition();
  }

  @Override
  public RecordState getState() {
    return this.record.getState();
  }

  @Override
  public <T extends Object> T getValue(final int index) {
    return this.record.getValue(index);
  }

  @Override
  public int hashCode() {
    return this.record.hashCode();
  }

  @Override
  public void setState(final RecordState state) {
    this.record.setState(state);
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    return this.record.setValue(index, value);
  }
}

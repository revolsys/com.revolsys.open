package com.revolsys.swing.undo;

import com.revolsys.record.Record;

public class SetRecordFieldValueUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final String fieldName;

  private final Object newValue;

  private final Object oldValue;

  private final Record record;

  public SetRecordFieldValueUndo(final Record record, final String fieldName, final Object oldValue,
    final Object newValue) {
    if (record == null) {
      throw new IllegalArgumentException("record must not be null");
    }
    if (fieldName == null) {
      throw new IllegalArgumentException("fieldName must not be null");
    }
    this.record = record;
    this.fieldName = fieldName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public boolean canRedo() {
    final Record record = this.record;
    if (super.canRedo() && record != null && !record.isDeleted()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (this.record != null && !this.record.isDeleted() && super.canUndo()) {
      if (this.record.equalValue(this.fieldName, this.newValue)) {
        return true;
      }
    }
    return false;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public Record getRecord() {
    return this.record;
  }

  @Override
  protected void redoDo() {
    setValue(this.newValue);
  }

  protected boolean setValue(final Object value) {
    if (this.record == null || this.record.isDeleted()) {
      return false;
    } else {
      return this.record.setValue(this.fieldName, value);
    }
  }

  @Override
  public String toString() {
    return this.fieldName + "=" + this.oldValue + " -> " + this.newValue + "\n" + this.record;
  }

  @Override
  protected void undoDo() {
    setValue(this.oldValue);
  }
}

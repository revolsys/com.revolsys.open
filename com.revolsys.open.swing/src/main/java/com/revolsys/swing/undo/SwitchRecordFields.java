package com.revolsys.swing.undo;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.data.equals.Equals;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class SwitchRecordFields extends AbstractUndoableEdit {

  private static final long serialVersionUID = 1L;

  private final String fieldName1;

  private final String fieldName2;

  private final LayerRecord record;

  private final Object value1;

  private final Object value2;

  public SwitchRecordFields(final LayerRecord record, final String fieldName1, final Object value1,
    final String fieldName2, final Object value2) {
    this.record = record;
    this.fieldName1 = fieldName1;
    this.value1 = value1;
    this.fieldName2 = fieldName2;
    this.value2 = value2;
  }

  public SwitchRecordFields(final LayerRecord record, final String fieldName1,
    final String fieldName2) {
    this.record = record;
    this.fieldName1 = fieldName1;
    this.fieldName2 = fieldName2;
    this.value1 = record.getValue(fieldName1);
    this.value2 = record.getValue(fieldName2);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Object value1 = this.record.getValue(this.fieldName1);
      final Object value2 = this.record.getValue(this.fieldName2);
      if (!Equals.equal(value1, value2)) {
        if (Equals.equal(this.value1, value1)) {
          if (Equals.equal(this.value2, value2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Object value1 = this.record.getValue(this.fieldName1);
      final Object value2 = this.record.getValue(this.fieldName2);
      if (Equals.equal(this.value1, value2)) {
        if (Equals.equal(this.value2, value1)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    final Map<String, Object> newValues = new LinkedHashMap<>();
    newValues.put(this.fieldName1, this.value2);
    newValues.put(this.fieldName2, this.value1);
    this.record.setValues(newValues);
  }

  @Override
  protected void doUndo() {
    final Map<String, Object> newValues = new LinkedHashMap<>();
    newValues.put(this.fieldName1, this.value1);
    newValues.put(this.fieldName2, this.value2);
    this.record.setValues(newValues);
  }

  @Override
  public String toString() {
    return "switch " + this.fieldName1 + "=" + this.value1 + " and " + this.fieldName2 + "="
      + this.value2;
  }
}

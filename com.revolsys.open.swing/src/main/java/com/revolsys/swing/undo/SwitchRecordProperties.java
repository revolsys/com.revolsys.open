package com.revolsys.swing.undo;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.map.layer.record.BatchUpdate;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;

public class SwitchRecordProperties extends AbstractUndoableEdit {

  private static final long serialVersionUID = 1L;

  private final LayerRecord record;

  private final String propertyName1;

  private final String propertyName2;

  private final Object value1;

  private final Object value2;

  public SwitchRecordProperties(final LayerRecord record, final String propertyName1,
    final Object value1, final String propertyName2, final Object value2) {
    this.record = record;
    this.propertyName1 = propertyName1;
    this.value1 = value1;
    this.propertyName2 = propertyName2;
    this.value2 = value2;
  }

  public SwitchRecordProperties(final LayerRecord record, final String propertyName1,
    final String propertyName2) {
    this.record = record;
    this.propertyName1 = propertyName1;
    this.propertyName2 = propertyName2;
    this.value1 = Property.get(record, propertyName1);
    this.value2 = Property.get(record, propertyName2);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Object value1 = Property.get(this.record, this.propertyName1);
      final Object value2 = Property.get(this.record, this.propertyName2);
      if (!EqualsRegistry.equal(value1, value2)) {
        if (EqualsRegistry.equal(this.value1, value1)) {
          if (EqualsRegistry.equal(this.value2, value2)) {
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
      final Object value1 = Property.get(this.record, this.propertyName1);
      final Object value2 = Property.get(this.record, this.propertyName2);
      if (EqualsRegistry.equal(this.value1, value2)) {
        if (EqualsRegistry.equal(this.value2, value1)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    try (
      BatchUpdate batchUpdate = new BatchUpdate(this.record)) {
      Property.set(this.record, this.propertyName1, this.value2);
      Property.set(this.record, this.propertyName2, this.value1);
    }
  }

  @Override
  protected void doUndo() {
    try (
      BatchUpdate batchUpdate = new BatchUpdate(this.record)) {
      Property.set(this.record, this.propertyName1, this.value1);
      Property.set(this.record, this.propertyName2, this.value2);
    }
  }

  @Override
  public String toString() {
    return "switch " + this.propertyName1 + "=" + this.value1 + " and " + this.propertyName2 + "="
      + this.value2;
  }
}

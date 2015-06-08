package com.revolsys.swing.undo;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.equals.MapEquals;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class SetRecordValuesUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final LayerRecord record;

  private final Map<String, Object> originalValues = new HashMap<String, Object>();

  private final Map<String, Object> newValues = new HashMap<String, Object>();

  public SetRecordValuesUndo(final LayerRecord record, final Map<String, Object> newValues) {
    this.record = record;
    if (record != null) {
      this.originalValues.putAll(record);
    }
    if (newValues != null) {
      this.newValues.putAll(newValues);
    }
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      return MapEquals.equalMap1Keys(this.record, this.originalValues);
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      return MapEquals.equalMap1Keys(this.record, this.newValues);
    }
    return false;
  }

  @Override
  protected void doRedo() {
    if (this.record != null) {
      this.record.getLayer().replaceValues(this.record, this.newValues);
    }
  }

  @Override
  protected void doUndo() {
    if (this.record != null) {
      this.record.getLayer().replaceValues(this.record, this.originalValues);
    }
  }

  @Override
  public String toString() {
    return "Set record values";
  }
}

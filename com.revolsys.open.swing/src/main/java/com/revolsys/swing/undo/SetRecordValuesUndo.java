package com.revolsys.swing.undo;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.data.equals.MapEquals;

public class SetRecordValuesUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final DataObject record;

  private final Map<String, Object> originalValues = new HashMap<String, Object>();

  private final Map<String, Object> newValues = new HashMap<String, Object>();

  public SetRecordValuesUndo(final DataObject record,
    final Map<String, Object> newValues) {
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
      return MapEquals.equalMap1Keys(record, originalValues);
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      return MapEquals.equalMap1Keys(record, newValues);
    }
    return false;
  }

  @Override
  protected void doRedo() {
    if (record != null) {
      record.setValues(newValues);
    }
  }

  @Override
  protected void doUndo() {
    if (record != null) {
      record.setValues(originalValues);
    }
  }

  @Override
  public String toString() {
    return "Set record values";
  }
}

package com.revolsys.swing.undo;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.collection.map.Maps;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class SetRecordValuesUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final Map<String, Object> newValues = new HashMap<>();

  private final Map<String, Object> originalValues = new HashMap<>();

  private final LayerRecord record;

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
      return Maps.equalMap1Keys(this.record, this.originalValues);
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      return Maps.equalMap1Keys(this.record, this.newValues);
    }
    return false;
  }

  @Override
  protected void redoDo() {
    if (this.record != null) {
      final AbstractRecordLayer layer = this.record.getLayer();
      layer.replaceValues(this.record, this.newValues);
    }
  }

  @Override
  public String toString() {
    return "Set record values";
  }

  @Override
  protected void undoDo() {
    if (this.record != null) {
      final AbstractRecordLayer layer = this.record.getLayer();
      layer.replaceValues(this.record, this.originalValues);
    }
  }
}

package com.revolsys.swing.undo;

import com.revolsys.data.record.Record;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class CreateRecordUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final Record record;

  private LayerRecord layerRecord;

  private final AbstractRecordLayer layer;

  public CreateRecordUndo(final AbstractRecordLayer layer,
    final Record record) {
    this.layer = layer;
    this.record = record;
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (record != null) {
        if (layerRecord == null) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (record != null) {
        if (layerRecord != null) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    if (record != null) {
      layerRecord = layer.createRecord(record);
      layer.saveChanges(layerRecord);
      layer.addSelectedRecords(layerRecord);
    }
  }

  @Override
  protected void doUndo() {
    if (record != null) {
      layer.deleteRecord(layerRecord);
      layer.saveChanges(layerRecord);
      layerRecord = null;
    }
  }

  @Override
  public String toString() {
    return "Create Record";
  }
}

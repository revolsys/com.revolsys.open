package com.revolsys.swing.undo;

import com.revolsys.data.record.Record;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class CreateRecordUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  private LayerRecord layerRecord;

  private final Record record;

  public CreateRecordUndo(final AbstractRecordLayer layer, final Record record) {
    this.layer = layer;
    this.record = record;
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (this.record != null) {
        if (this.layerRecord == null) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (this.record != null) {
        if (this.layerRecord != null) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    if (this.record != null) {
      this.layerRecord = this.layer.createRecord(this.record);
      this.layer.saveChanges(this.layerRecord);
      this.layer.addSelectedRecords(this.layerRecord);
    }
  }

  @Override
  protected void doUndo() {
    if (this.record != null) {
      this.layer.deleteRecord(this.layerRecord);
      this.layer.saveChanges(this.layerRecord);
      this.layerRecord = null;
    }
  }

  @Override
  public String toString() {
    return "Create Record";
  }
}

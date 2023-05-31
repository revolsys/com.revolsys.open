package com.revolsys.swing.map.layer.record.component.recordmerge;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.undo.AbstractUndoableEdit;

class MergeCreateRecordUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  private LayerRecord layerRecord;

  private final MergeableRecord record;

  public MergeCreateRecordUndo(final AbstractRecordLayer layer, final MergeableRecord record) {
    this.layer = layer;
    this.record = record;
  }

  @Override
  protected void redoDo() {
    if (this.layerRecord == null) {
      this.layerRecord = this.layer.newMergedRecord(this.record);
      this.layerRecord.saveChanges();
      this.layer.addSelectedRecords(this.layerRecord);
    }
  }

  @Override
  public String toString() {
    return "Create Mergeable Record";
  }

  @Override
  protected void undoDo() {
    if (this.layerRecord != null) {
      this.layer.deleteRecordAndSaveChanges(this.layerRecord);
      this.layerRecord = null;
    }
  }
}

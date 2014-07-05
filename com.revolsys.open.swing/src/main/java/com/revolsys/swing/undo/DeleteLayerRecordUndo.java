package com.revolsys.swing.undo;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class DeleteLayerRecordUndo extends AbstractUndoableEdit {

  private static final long serialVersionUID = 1L;

  private final LayerRecord record;

  private Map<String, Object> values;

  public DeleteLayerRecordUndo(final LayerRecord record) {
    this.record = record;
    if (record != null) {
      this.values = new HashMap<String, Object>(record);
    }
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (this.record != null) {
        final AbstractRecordLayer layer = this.record.getLayer();
        if (layer != null) {
          return !layer.isDeleted(this.record);
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (this.record != null) {
        final AbstractRecordLayer layer = this.record.getLayer();
        if (layer != null) {
          return layer.isDeleted(this.record);
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    if (this.record != null) {
      final AbstractRecordLayer layer = this.record.getLayer();
      if (layer != null) {
        layer.deleteRecords(this.record);
        layer.unSelectRecords(this.record);
      }
    }
  }

  @Override
  protected void doUndo() {
    if (this.record != null) {
      final LayerRecord sourceRecord = this.record.revertChanges();
      sourceRecord.setValues(this.values);
      final AbstractRecordLayer layer = sourceRecord.getLayer();
      layer.addSelectedRecords(sourceRecord);
    }
  }
}

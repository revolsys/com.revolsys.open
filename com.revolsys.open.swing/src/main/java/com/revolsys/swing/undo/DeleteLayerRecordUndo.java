package com.revolsys.swing.undo;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

public class DeleteLayerRecordUndo extends AbstractUndoableEdit {

  private static final long serialVersionUID = 1L;

  private final LayerDataObject record;

  private Map<String, Object> values;

  public DeleteLayerRecordUndo(final LayerDataObject record) {
    this.record = record;
    if (record != null) {
      this.values = new HashMap<String, Object>(record);
    }
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (record != null) {
        final AbstractDataObjectLayer layer = record.getLayer();
        if (layer != null) {
          return !layer.isDeleted(record);
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (record != null) {
        final AbstractDataObjectLayer layer = record.getLayer();
        if (layer != null) {
          return layer.isDeleted(record);
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    if (record != null) {
      final AbstractDataObjectLayer layer = record.getLayer();
      if (layer != null) {
        layer.deleteRecords(record);
        layer.removeSelectedRecords(record);
      }
    }
  }

  @Override
  protected void doUndo() {
    if (record != null) {
      final LayerDataObject sourceRecord = record.revertChanges();
      sourceRecord.setValues(values);
      final AbstractDataObjectLayer layer = sourceRecord.getLayer();
      layer.addSelectedRecords(sourceRecord);
    }
  }
}

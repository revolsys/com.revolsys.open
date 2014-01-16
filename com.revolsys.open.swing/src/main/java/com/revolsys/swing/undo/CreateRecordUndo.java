package com.revolsys.swing.undo;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

public class CreateRecordUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final DataObject record;

  private LayerDataObject layerRecord;

  private final AbstractDataObjectLayer layer;

  public CreateRecordUndo(final AbstractDataObjectLayer layer,
    final DataObject record) {
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

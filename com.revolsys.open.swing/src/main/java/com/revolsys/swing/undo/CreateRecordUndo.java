package com.revolsys.swing.undo;

import java.util.Map;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;

public class CreateRecordUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  private LayerRecord layerRecord;

  private final Map<String, Object> newValues;

  public CreateRecordUndo(final AbstractRecordLayer layer, final Map<String, Object> newValues) {
    this.layer = layer;
    this.newValues = newValues;
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (Property.hasValue(this.newValues)) {
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
      if (Property.hasValue(this.newValues)) {
        if (this.layerRecord != null) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    if (Property.hasValue(this.newValues) && this.layerRecord == null) {
      this.layerRecord = this.layer.newLayerRecord(this.newValues);
      this.layer.saveChanges(this.layerRecord);
      this.layer.addSelectedRecords(this.layerRecord);
    }
  }

  @Override
  public String toString() {
    return "Create Record";
  }

  @Override
  protected void undoDo() {
    if (this.layerRecord != null) {
      this.layer.deleteRecordAndSaveChanges(this.layerRecord);
      this.layerRecord = null;
    }
  }
}

package com.revolsys.swing.undo;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

/**
 * Deletes a record if it has not already been deleted. For the undo the original values are saved
 * and a new record created with those original values. This prevents keeping the original
 * proxy record around for long periods of time.
 */
public class DeleteLayerRecordUndo extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private LayerRecord record;

  private Map<String, Object> originalValues;

  private AbstractRecordLayer layer;

  public DeleteLayerRecordUndo(final LayerRecord record) {
    if (record != null) {
      this.layer = record.getLayer();
      this.record = record;
      this.originalValues = new HashMap<>(record);
    }
  }

  @Override
  protected void redoDo() {
    if (this.record != null && !this.layer.isDeleted(this.record)) {
      this.layer.deleteRecordAndSaveChanges(this.record);
      this.record = null;
    }
  }

  @Override
  protected void undoDo() {
    if (this.record == null && this.layer != null) {
      final LayerRecord newRecord = this.layer.newLayerRecord(this.originalValues);
      this.layer.saveChanges(newRecord);
      this.record = newRecord;
    }
  }
}

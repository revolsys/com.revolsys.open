package com.revolsys.swing.undo;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class ReverseRecordUndo extends AbstractUndoableEdit {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Geometry oldValue;

  private final LayerRecord record;

  public ReverseRecordUndo(final LayerRecord record) {
    this.record = record;
    this.oldValue = record.getGeometry();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Geometry value = this.record.getGeometry();
      if (DataType.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Geometry value = this.record.getGeometry();
      if (DataType.equal(value.reverse(), this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    DirectionalFields.reverseRecord(this.record);
  }

  @Override
  public String toString() {
    return "Reverse record";
  }

  @Override
  protected void undoDo() {
    DirectionalFields.reverseRecord(this.record);
  }
}

package com.revolsys.swing.undo;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class ReverseRecordUndo extends AbstractUndoableEdit {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final LayerRecord record;

  private final Geometry oldValue;

  public ReverseRecordUndo(final LayerRecord record) {
    this.record = record;
    this.oldValue = record.getGeometry();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Geometry value = this.record.getGeometry();
      if (EqualsRegistry.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Geometry value = this.record.getGeometry();
      if (EqualsRegistry.equal(value.reverse(), this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    DirectionalAttributes.reverse(this.record);
  }

  @Override
  protected void doUndo() {
    DirectionalAttributes.reverse(this.record);
  }

  @Override
  public String toString() {
    return "Reverse record";
  }
}

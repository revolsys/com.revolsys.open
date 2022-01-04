package com.revolsys.swing.map.overlay.record.geometryeditor;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.simplify.DouglasPeuckerSimplifier;
import com.revolsys.record.Record;
import com.revolsys.swing.undo.AbstractUndoableEdit;

public class GeneralizeVerticesGeometryUndoEdit extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final Geometry oldGeometry;

  private final Geometry newGeometry;

  private final Record record;

  public GeneralizeVerticesGeometryUndoEdit(final Record record, final double tolerance) {
    this.record = record;
    this.oldGeometry = record.getGeometry();
    this.newGeometry = DouglasPeuckerSimplifier.simplify(this.oldGeometry, tolerance, true);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (DataType.equal(this.record.getGeometry(), this.oldGeometry)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (DataType.equal(this.record.getGeometry(), this.newGeometry)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    this.record.setGeometryValue(this.newGeometry);
  }

  @Override
  protected void undoDo() {
    this.record.setGeometryValue(this.oldGeometry);
  }
}

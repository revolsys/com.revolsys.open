package com.revolsys.swing.map.overlay.record.geometryeditor;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.swing.undo.AbstractUndoableEdit;

public class MoveGeometryUndoEdit extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final GeometryEditor<?> geometryEditor;

  private final double deltaX;

  private final double deltaY;

  private final Point oldPoint;

  private final Point newPoint;

  public MoveGeometryUndoEdit(final GeometryEditor<?> geometryEditor, final double deltaX,
    final double deltaY) {
    this.geometryEditor = geometryEditor;
    this.deltaX = deltaX;
    this.deltaY = deltaY;
    this.oldPoint = geometryEditor.getPoint();
    this.newPoint = this.oldPoint.add(deltaX, deltaY);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (this.geometryEditor.getPoint().equals(2, this.oldPoint)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (this.geometryEditor.getPoint().equals(2, this.newPoint)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    this.geometryEditor.move(this.deltaX, this.deltaY);
  }

  @Override
  protected void undoDo() {
    this.geometryEditor.move(-this.deltaX, -this.deltaY);
  }
}

package com.revolsys.swing.map.overlay.record.geometryeditor;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.swing.undo.AbstractUndoableEdit;

public class SetVertexUndoEdit extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final GeometryEditor<?> geometryEditor;

  private final int[] vertexId;

  private final Point newPoint;

  private final Point oldPoint;

  public SetVertexUndoEdit(final GeometryEditor<?> geometryEditor, final int[] vertexId,
    final Point newPoint) {
    this.geometryEditor = geometryEditor;
    this.vertexId = vertexId;
    this.newPoint = newPoint;
    this.oldPoint = geometryEditor.getVertex(vertexId).newPoint();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      if (this.geometryEditor.equalsVertex(this.vertexId, this.oldPoint)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      if (this.geometryEditor.equalsVertex(this.vertexId, this.newPoint)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    this.geometryEditor.setVertex(this.vertexId, this.newPoint);
  }

  @Override
  protected void undoDo() {
    this.geometryEditor.setVertex(this.vertexId, this.oldPoint);
  }
}

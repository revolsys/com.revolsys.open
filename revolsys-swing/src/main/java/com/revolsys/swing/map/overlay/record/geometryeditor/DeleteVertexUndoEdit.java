package com.revolsys.swing.map.overlay.record.geometryeditor;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.swing.undo.AbstractUndoableEdit;

public class DeleteVertexUndoEdit extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final GeometryEditor<?> geometryEditor;

  private final int[] vertexId;

  private final Point oldPoint;

  private final int vertexCount;

  public DeleteVertexUndoEdit(final GeometryEditor<?> geometryEditor, final int[] vertexId) {
    this.geometryEditor = geometryEditor;
    this.vertexId = vertexId;
    this.oldPoint = geometryEditor.getVertex(vertexId).newPoint();
    this.vertexCount = getCurrentVertexCount();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final int currentVertexCount = getCurrentVertexCount();
      if (this.vertexCount == currentVertexCount) {
        if (this.geometryEditor.equalsVertex(this.vertexId, this.oldPoint)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final int currentVertexCount = getCurrentVertexCount();
      if (this.vertexCount - 1 == currentVertexCount) {
        return true;
      }
    }
    return false;
  }

  private int getCurrentVertexCount() {
    return this.geometryEditor.getVertexCount(this.vertexId, this.vertexId.length - 1);
  }

  @Override
  protected void redoDo() {
    this.geometryEditor.deleteVertex(this.vertexId);
  }

  @Override
  protected void undoDo() {
    this.geometryEditor.insertVertex(this.vertexId, this.oldPoint);
  }
}

package com.revolsys.swing.map.overlay.record.geometryeditor;

import java.util.Arrays;

import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.swing.undo.AbstractUndoableEdit;

public class AppendVertexUndoEdit extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final GeometryEditor<?> geometryEditor;

  private final int[] geometryId;

  private final Point point;

  private final int vertexCount;

  private final GeometryDataType<?, ?> partDataType;

  public AppendVertexUndoEdit(final GeometryEditor<?> geometryEditor, final int[] geometryId,
    final GeometryDataType<?, ?> partDataType, final Point point) {
    this.geometryEditor = geometryEditor;
    this.geometryId = geometryId;
    this.partDataType = partDataType;
    this.point = point;
    this.vertexCount = geometryEditor.getVertexCount(geometryId);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final int vertexCount = this.geometryEditor.getVertexCount();
      if (vertexCount == this.vertexCount) {
        if (!this.geometryEditor.equalsVertex(2, this.geometryId, this.vertexCount - 1,
          this.point)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final int vertexCount = this.geometryEditor.getVertexCount();
      if (vertexCount == this.vertexCount + 1) {
        if (this.geometryEditor.equalsVertex(2, this.geometryId, this.vertexCount, this.point)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    this.geometryEditor.appendVertex(this.geometryId, this.partDataType, this.point);
  }

  @Override
  protected void undoDo() {
    final int[] childVertexId = Arrays.copyOf(this.geometryId, this.geometryId.length + 1);
    childVertexId[this.geometryId.length] = this.vertexCount;
    this.geometryEditor.deleteVertex(childVertexId);
  }
}

package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.util.GeometryEditor;

public class GeometryVertexInserter {
  private static class InsertVertexOperation extends
    GeometryEditor.CoordinateOperation {
    private final LineString line;

    private final int segIndex;

    private final Coordinates newVertex;

    public InsertVertexOperation(final LineString line, final int segIndex,
      final Coordinates newVertex) {
      this.line = line;
      this.segIndex = segIndex;
      this.newVertex = newVertex;
    }

    @Override
    public Coordinates[] edit(final Coordinates[] coords,
      final Geometry geometry) {
      if (geometry != line) {
        return coords;
      }

      final Coordinates[] newPts = new Coordinates[coords.length + 1];
      for (int i = 0; i < coords.length; i++) {
        final int actualIndex = i > segIndex ? i + 1 : i;
        newPts[actualIndex] = coords[i].cloneCoordinates();
      }
      newPts[segIndex + 1] = newVertex.cloneCoordinates();
      return newPts;
    }
  }

  public static Geometry insert(final Geometry geom, final LineString line,
    final int segIndex, final Coordinates newVertex) {
    final GeometryEditor editor = new GeometryEditor();
    return editor.edit(geom, new InsertVertexOperation(line, segIndex,
      newVertex));
  }

}

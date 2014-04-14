package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.util.GeometryEditor;

public class GeometryVertexMover {

  private static class MoveVertexOperation extends
    GeometryEditor.CoordinateOperation {
    private final Coordinates fromLoc;

    private final Coordinates toLoc;

    public MoveVertexOperation(final Coordinates fromLoc,
      final Coordinates toLoc) {
      this.fromLoc = fromLoc;
      this.toLoc = toLoc;
    }

    @Override
    public Coordinates[] edit(final Coordinates[] coords,
      final Geometry geometry) {
      final Coordinates[] newPts = new Coordinates[coords.length];
      for (int i = 0; i < coords.length; i++) {
        newPts[i] = (coords[i].equals2d(fromLoc)) ? toLoc.cloneCoordinates()
          : coords[i].cloneCoordinates();

      }
      return newPts;
    }
  }

  public static Geometry move(final Geometry geom, final Coordinates fromLoc,
    final Coordinates toLoc) {
    final GeometryEditor editor = new GeometryEditor();
    return editor.edit(geom, new MoveVertexOperation(fromLoc, toLoc));
  }

}

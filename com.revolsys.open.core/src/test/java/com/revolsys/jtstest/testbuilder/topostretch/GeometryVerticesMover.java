package com.revolsys.jtstest.testbuilder.topostretch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.util.GeometryEditor;

public class GeometryVerticesMover {
  private class MoveVerticesOperation extends
    GeometryEditor.CoordinateOperation {
    private final Map<Coordinates, Coordinates> moves;

    public MoveVerticesOperation(final Map<Coordinates, Coordinates> moves) {
      this.moves = moves;
    }

    @Override
    public Coordinates[] edit(final Coordinates[] coords,
      final Geometry geometry) {
      final Coordinates[] newPts = new Coordinates[coords.length];
      for (int i = 0; i < coords.length; i++) {
        newPts[i] = movedPt(coords[i]);
      }
      return newPts;
    }

    private Coordinates movedPt(final Coordinates orig) {
      final Coordinates newLoc = moves.get(orig);
      if (newLoc == null) {
        return orig;
      }
      final Coordinates mod = newLoc.cloneCoordinates();
      modifiedCoords.add(mod);
      return mod;
    }
  }

  public static Geometry move(final Geometry geom,
    final Map<Coordinates, Coordinates> moves) {
    final GeometryVerticesMover mover = new GeometryVerticesMover(geom, moves);
    return mover.move();
  }

  private final Geometry geom;

  private final Map<Coordinates, Coordinates> moves;

  private final List<Coordinates> modifiedCoords = new ArrayList<Coordinates>();

  public GeometryVerticesMover(final Geometry geom,
    final Map<Coordinates, Coordinates> moves) {
    this.geom = geom;
    this.moves = moves;
  }

  public List<Coordinates> getModifiedCoordinates() {
    return modifiedCoords;
  }

  public Geometry move() {
    final GeometryEditor editor = new GeometryEditor();
    final MoveVerticesOperation op = new MoveVerticesOperation(moves);
    final Geometry movedGeom = editor.edit(geom, new MoveVerticesOperation(
      moves));
    return movedGeom;
  }

}

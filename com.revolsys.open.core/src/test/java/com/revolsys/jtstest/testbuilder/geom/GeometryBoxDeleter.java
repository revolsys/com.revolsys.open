package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.util.GeometryEditor;

/**
 * Deletes vertices or components from a geometry
 * which lie inside a given box.
 * If the box completely contains one or more components
 * (including polygon holes), those components are deleted
 * and the operation stops.
 * Otherwise if the box contains a subset of vertices 
 * from a component, those vertices are deleted. 
 * When deleting vertices only <i>one</i> component of the geometry
 * is modified (the first one found which has vertices in the box).
 * 
 * @author Martin Davis
 *
 */
public class GeometryBoxDeleter {
  private static class BoxDeleteComponentOperation implements
    GeometryEditor.GeometryEditorOperation {
    private final BoundingBox env;

    private boolean isEdited = false;

    public BoxDeleteComponentOperation(final BoundingBox env) {
      this.env = env;
    }

    @Override
    public Geometry edit(final Geometry geometry, final GeometryFactory factory) {
      // Allow any number of components to be deleted
      // if (isEdited) return geometry;
      if (env.contains(geometry.getBoundingBox())) {
        isEdited = true;
        return null;
      }
      return geometry;
    }

    public boolean isEdited() {
      return isEdited;
    }
  }

  private static class BoxDeleteVertexOperation extends
    GeometryEditor.CoordinateOperation {
    private final BoundingBox env;

    private boolean isEdited = false;

    public BoxDeleteVertexOperation(final BoundingBox env) {
      this.env = env;
    }

    @Override
    public Coordinates[] edit(final Coordinates[] coords,
      final Geometry geometry) {
      if (isEdited) {
        return coords;
      }
      if (!hasVertexInBox(coords)) {
        return coords;
        // only delete vertices of first component found
      }

      int minLen = 2;
      if (geometry instanceof LinearRing) {
        minLen = 4;
      }

      final Coordinates[] newPts = new Coordinates[coords.length];
      int newIndex = 0;
      for (int i = 0; i < coords.length; i++) {
        if (!env.contains(coords[i])) {
          newPts[newIndex++] = coords[i];
        }
      }
      final Coordinates[] nonNullPts = CoordinateArrays.removeNull(newPts);
      Coordinates[] finalPts = nonNullPts;

      // close ring if required
      if (geometry instanceof LinearRing) {
        if (nonNullPts.length > 1
          && !nonNullPts[nonNullPts.length - 1].equals2d(nonNullPts[0])) {
          final Coordinates[] ringPts = new Coordinates[nonNullPts.length + 1];
          CoordinateArrays.copyDeep(nonNullPts, 0, ringPts, 0,
            nonNullPts.length);
          ringPts[ringPts.length - 1] = new Coordinate(ringPts[0]);
          finalPts = ringPts;
        }
      }

      // don't change if would make geometry invalid
      if (finalPts.length < minLen) {
        return coords;
      }

      isEdited = true;
      return finalPts;
    }

    private boolean hasVertexInBox(final Coordinates[] coords) {
      for (int i = 0; i < coords.length; i++) {
        if (env.contains(coords[i])) {
          return true;
        }
      }
      return false;
    }

    public boolean isEdited() {
      return isEdited;
    }
  }

  public static Geometry delete(final Geometry geom, final BoundingBox env) {
    final Geometry gComp = deleteComponents(geom, env);
    if (gComp != null) {
      return gComp;
    }

    // otherwise, try and edit vertices
    final Geometry gVert = deleteVertices(geom, env);
    if (gVert != null) {
      return gVert;
    }

    // no edits - return original
    return geom;
  }

  private static Geometry deleteComponents(final Geometry geom,
    final BoundingBox env) {
    final GeometryEditor editor = new GeometryEditor();
    final BoxDeleteComponentOperation compOp = new BoxDeleteComponentOperation(
      env);
    final Geometry compEditGeom = editor.edit(geom, compOp);
    if (compOp.isEdited()) {
      return compEditGeom;
    }
    return null;
  }

  private static Geometry deleteVertices(final Geometry geom, final BoundingBox env) {
    final GeometryEditor editor = new GeometryEditor();
    final BoxDeleteVertexOperation vertexOp = new BoxDeleteVertexOperation(env);
    final Geometry vertexEditGeom = editor.edit(geom, vertexOp);
    if (vertexOp.isEdited()) {
      return vertexEditGeom;
    }
    return null;
  }

}

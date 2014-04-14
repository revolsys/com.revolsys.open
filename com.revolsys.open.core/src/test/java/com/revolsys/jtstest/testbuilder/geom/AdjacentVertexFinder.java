package com.revolsys.jtstest.testbuilder.geom;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;

/**
 * Locates all vertices in a geometry which are adjacent 
 * to a given vertex.
 * 
 * @author mbdavis
 *
 */
public class AdjacentVertexFinder {
  static class AdjacentVertexFilter implements CoordinateSequenceFilter {
    private final Coordinates basePt;

    private final List adjVerts = new ArrayList();

    public AdjacentVertexFilter(final Coordinates basePt) {
      this.basePt = basePt;
    }

    @Override
    public void filter(final CoordinatesList seq, final int i) {
      final Coordinates p = seq.getCoordinate(i);
      if (!p.equals2d(basePt)) {
        return;
      }

      if (i > 0) {
        adjVerts.add(seq.getCoordinate(i - 1));
      }
      if (i < seq.size() - 1) {
        adjVerts.add(seq.getCoordinate(i + 1));
      }
    }

    public Coordinates[] getVertices() {
      return CoordinateArrays.toCoordinateArray(adjVerts);
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public boolean isGeometryChanged() {
      return false;
    }
  }

  public static Coordinates[] findVertices(final Geometry geom,
    final Coordinates testPt) {
    final AdjacentVertexFinder finder = new AdjacentVertexFinder(geom);
    return finder.getVertices(testPt);
  }

  private final Geometry geom;

  private Coordinates vertexPt;

  private final int vertexIndex = -1;

  public AdjacentVertexFinder(final Geometry geom) {
    this.geom = geom;
  }

  public int getIndex() {
    return vertexIndex;
  }

  public Coordinates[] getVertices(final Coordinates testPt) {
    final AdjacentVertexFilter filter = new AdjacentVertexFilter(testPt);
    geom.apply(filter);
    return filter.getVertices();
  }

}

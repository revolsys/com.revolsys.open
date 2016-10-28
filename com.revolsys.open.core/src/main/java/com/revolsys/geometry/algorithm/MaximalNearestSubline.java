package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.LineStringDoubleBuilder;

/**
 * Computes the Maximal Nearest Subline of a given linestring relative to
 * another linestring. The Maximal Nearest Subline of A relative to B is the
 * shortest subline of A which contains all the points of A which are the
 * nearest points to the points in B. This effectively "trims" the ends of A
 * which are not near to B.
 * <p>
 * An exact computation of the MNS would require computing a line Voronoi. For
 * this reason, the algorithm used in this class is heuristic-based. It may
 * compute a geometry which is shorter than the actual MNS.
 */
public class MaximalNearestSubline {

  public static LineString getMaximalNearestSubline(final LineString line1, LineString line2) {
    line2 = line2.convertGeometry(line1.getGeometryFactory());
    LineStringLocation maxInterval1 = null;

    LineStringLocation maxInterval2 = null;

    /**
    * The basic strategy is to pick test points on B and find their nearest
    * point on A. The interval containing these nearest points is approximately
    * the MaximalNeareastSubline of A.
    */

    {
      final int vertexCount2 = line2.getVertexCount();
      for (int vertexIndex = 0; vertexIndex < vertexCount2; vertexIndex++) {
        final double x = line2.getX(vertexIndex);
        final double y = line2.getY(vertexIndex);
        final LineStringLocation location = line1.getLineStringLocation(x, y);
        if (maxInterval1 == null || location.compareTo(maxInterval1) < 0) {
          maxInterval1 = location;
        }
        if (maxInterval2 == null || location.compareTo(maxInterval2) > 0) {
          maxInterval2 = location;
        }
      }
    }

    /**
    * Heuristic #2: find the nearest point on B to all vertices of A and use
    * those points of B as test points. For efficiency use only vertices of A
    * outside current max interval.
    */
    {
      final int vertexCount1 = line1.getVertexCount();
      for (int vertexIndex = 0; vertexIndex < vertexCount1; vertexIndex++) {
        final double x = line1.getX(vertexIndex);
        final double y = line1.getY(vertexIndex);

        if (vertexIndex <= maxInterval1.getSegmentIndex()
          || vertexIndex > maxInterval2.getSegmentIndex()) {
          final LineStringLocation location2 = line2.getLineStringLocation(x, y);
          final Point point2 = location2.getPoint();
          final double x2 = point2.getX();
          final double y2 = point2.getY();
          final LineStringLocation location = line1.getLineStringLocation(x2, y2);
          if (maxInterval1 == null || location.compareTo(maxInterval1) < 0) {
            maxInterval1 = location;
          }
          if (maxInterval2 == null || location.compareTo(maxInterval2) > 0) {
            maxInterval2 = location;
          }
        }
      }
    }

    return getSubline(line1, maxInterval1, maxInterval2);
  }

  public static LineString getSubline(final LineString line, final LineStringLocation start,
    final LineStringLocation end) {
    final LineStringDoubleBuilder lineBuilder = new LineStringDoubleBuilder(
      line.getGeometryFactory(), line.getAxisCount());

    int vertexIndexFrom = start.getSegmentIndex();
    if (start.getSegmentFraction() > 0.0) {
      vertexIndexFrom += 1;
    }
    int vertexIndexTo = end.getSegmentIndex();
    if (end.getSegmentFraction() >= 1.0) {
      vertexIndexTo += 1;
    }

    if (!start.isVertex()) {
      final Point point = start.getPoint();
      lineBuilder.appendVertex(point, false);
    }

    for (int vertexIndex = vertexIndexFrom; vertexIndex <= vertexIndexTo; vertexIndex++) {
      final Point point = line.getPoint(vertexIndex);
      lineBuilder.appendVertex(point, false);
    }
    if (!end.isVertex()) {
      final Point point = end.getPoint();
      lineBuilder.appendVertex(point, false);
    }
    return lineBuilder.newLineString();
  }
}

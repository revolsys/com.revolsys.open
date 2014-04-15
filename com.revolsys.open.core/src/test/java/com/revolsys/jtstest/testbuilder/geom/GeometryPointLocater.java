package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponentFilter;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;

/**
 * Finds a vertex or a point on a segment of a Geometry
 * which lies within a tolerance of a given point.
 * 
 * @author Martin Davis
 *
 */
public class GeometryPointLocater {
  static class NearestSegmentLocationFilter implements GeometryComponentFilter {
    private double tolerance = 0.0;

    private final Coordinates testPt;

    private boolean vertexOnly = false;

    private Geometry component = null;

    private int segIndex = -1;

    private Coordinates nearestPt = null;

    private boolean isVertex = false;

    private final LineSegment seg = new LineSegment();

    public NearestSegmentLocationFilter(final Coordinates testPt,
      final boolean vertexOnly, final double tolerance) {
      this.testPt = testPt;
      this.tolerance = tolerance;
      this.vertexOnly = vertexOnly;
    }

    private void checkSegment(final LineString lineStr,
      final CoordinatesList seq, final int i) {
      final Coordinates p0 = seq.getCoordinate(i);
      final Coordinates p1 = seq.getCoordinate(i + 1);

      // if point matches endpoint ==> vertex match
      final double dist0 = p0.distance(testPt);
      final double dist1 = p1.distance(testPt);
      if (dist0 < tolerance) {
        nearestPt = p0;
        segIndex = i;
        isVertex = true;
        return;
      } else if (dist1 < tolerance) {
        nearestPt = p1;
        segIndex = i + 1;
        isVertex = true;
        return;
      }

      // check closeness to segment (if allowing segments)
      if (vertexOnly) {
        return;
      }

      seg.setP0(p0);
      seg.setP1(p1);
      final double segDist = seg.distance(testPt);
      if (segDist < tolerance) {
        nearestPt = seg.closestPoint(testPt);
        segIndex = i;
        isVertex = false;
      }
    }

    private void checkVertex(final LineString lineStr,
      final CoordinatesList seq, final int i) {
      final Coordinates p0 = seq.getCoordinate(i);

      final double dist0 = p0.distance(testPt);
      if (dist0 < tolerance) {
        nearestPt = p0;
        segIndex = i;
        isVertex = true;
      }
    }

    @Override
    public void filter(final Geometry geom) {
      if (!(geom instanceof LineString)) {
        return;
      }
      if (nearestPt != null) {
        return;
      }

      final LineString lineStr = (LineString)geom;
      final CoordinatesList seq = lineStr.getCoordinatesList();
      for (int i = 0; i < seq.size(); i++) {
        if (i != seq.size() - 1) {
          checkSegment(lineStr, seq, i);
        } else {
          checkVertex(lineStr, seq, i);
        }

        // check if done
        if (nearestPt != null) {
          // found matching location!
          component = lineStr;
          break;
        }
      }
    }

    public Geometry getComponent() {
      return component;
    }

    public Coordinates getCoordinate() {
      return nearestPt;
    }

    public int getIndex() {
      return segIndex;
    }

    public boolean isDone() {
      return nearestPt != null;
    }

    public boolean isGeometryChanged() {
      return false;
    }

    public boolean isVertex() {
      return isVertex;
    }
  }

  public static GeometryLocation locateNonVertexPoint(final Geometry geom,
    final Coordinates testPt, final double tolerance) {
    final GeometryPointLocater finder = new GeometryPointLocater(geom);
    final GeometryLocation geomLoc = finder.getLocation(testPt, false,
      tolerance);
    if (geomLoc == null) {
      return null;
    }
    if (geomLoc.isVertex()) {
      return null;
    }
    return geomLoc;
  }

  public static GeometryLocation locateVertex(final Geometry geom,
    final Coordinates testPt, final double tolerance) {
    final GeometryPointLocater finder = new GeometryPointLocater(geom);
    final GeometryLocation geomLoc = finder.getLocation(testPt, true, tolerance);
    if (geomLoc == null) {
      return null;
    }
    if (geomLoc.isVertex()) {
      return geomLoc;
    }
    return null;
  }

  private final Geometry geom;

  private Coordinates locationPt;

  private int segIndex = -1;

  private boolean isVertex = false;

  public GeometryPointLocater(final Geometry geom) {
    this.geom = geom;
  }

  public int getIndex() {
    return segIndex;
  }

  public GeometryLocation getLocation(final Coordinates testPt,
    final boolean vertexOnly, final double tolerance) {
    final NearestSegmentLocationFilter filter = new NearestSegmentLocationFilter(
      testPt, vertexOnly, tolerance);
    geom.apply(filter);

    locationPt = filter.getCoordinate();
    segIndex = filter.getIndex();
    isVertex = filter.isVertex();

    if (locationPt == null) {
      return null;
    }

    return new GeometryLocation(geom, filter.getComponent(), filter.getIndex(),
      filter.isVertex(), filter.getCoordinate());
  }

  public boolean isVertex() {
    return isVertex;
  }

}

package com.revolsys.gis.algorithm;

import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;

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

  public static LineString getMaximalNearestSubline(final LineString a,
    final LineString b) {
    final MaximalNearestSubline mns = new MaximalNearestSubline(a, b);
    final LineStringLocation[] interval = mns.getInterval();
    return getSubline(a, interval[0], interval[1]);
  }

  public static LineString getSubline(final LineString line,
    final LineStringLocation start, final LineStringLocation end) {
    final CoordinateList newCoordinates = new CoordinateList();

    int includedStartIndex = start.getSegmentIndex();
    if (start.getSegmentFraction() > 0.0) {
      includedStartIndex += 1;
    }
    int includedEndIndex = end.getSegmentIndex();
    if (end.getSegmentFraction() >= 1.0) {
      includedEndIndex += 1;
    }

    if (!start.isVertex()) {
      newCoordinates.add(start.getCoordinate(), false);
    }

    for (int i = includedStartIndex; i <= includedEndIndex; i++) {
      newCoordinates.add(line.getPoint(i), false);
    }
    if (!end.isVertex()) {
      newCoordinates.add(end.getCoordinate(), false);
    }
    if (Double.isNaN(newCoordinates.get(0).getX())) {
      newCoordinates.remove(0);
    }
    Point[] newCoordinateArray = newCoordinates.toCoordinateArray();
    /**
     * Ensure there is enough coordinates to build a valid line. Make a 2-point
     * line with duplicate coordinates, if necessary There will always be at
     * least one coordinate in the coordList.
     */
    if (newCoordinateArray.length <= 1) {
      newCoordinateArray = new Point[] {
        newCoordinateArray[0], newCoordinateArray[0]
      };
    }
    return line.getGeometryFactory().lineString(newCoordinateArray);
  }

  private final LineString a;

  private final LocationOfPoint aPtLocator;

  private final LineString b;

  private final LineStringLocation[] maxInterval = new LineStringLocation[2];

  /**
   * Create a new Maximal Nearest Subline of {@link LineString} <code>a</code>
   * relative to {@link LineString} <code>b</code>
   *
   * @param a the LineString on which to compute the subline
   * @param b the LineString to compute the subline relative to
   */
  public MaximalNearestSubline(final LineString a, final LineString b) {
    this.a = a;
    this.b = b;
    this.aPtLocator = new LocationOfPoint(a);
  }

  private void expandInterval(final LineStringLocation loc) {
    // expand maximal interval if this point is outside it
    if (this.maxInterval[0] == null || loc.compareTo(this.maxInterval[0]) < 0) {
      this.maxInterval[0] = loc;
    }
    if (this.maxInterval[1] == null || loc.compareTo(this.maxInterval[1]) > 0) {
      this.maxInterval[1] = loc;
    }
  }

  private void findNearestOnA(final Point bPt) {
    final LineStringLocation nearestLocationOnA = this.aPtLocator.locate(bPt);
    expandInterval(nearestLocationOnA);
  }

  /**
   * Computes the interval (range) containing the Maximal Nearest Subline.
   *
   * @return an array containing the minimum and maximum locations of the
   *         Maximal Nearest Subline of <code>A</code>
   */
  public LineStringLocation[] getInterval() {

    /**
     * The basic strategy is to pick test points on B and find their nearest
     * point on A. The interval containing these nearest points is approximately
     * the MaximalNeareastSubline of A.
     */

    // Heuristic #1: use every vertex of B as a test point
    final LineString bCoords = this.b;
    for (int ib = 0; ib < bCoords.getVertexCount(); ib++) {
      findNearestOnA(bCoords.getPoint(ib));
    }

    /**
     * Heuristic #2: find the nearest point on B to all vertices of A and use
     * those points of B as test points. For efficiency use only vertices of A
     * outside current max interval.
     */
    final LocationOfPoint bPtLocator = new LocationOfPoint(this.b);
    final LineString aCoords = this.a;
    for (int ia = 0; ia < aCoords.getVertexCount(); ia++) {
      if (isOutsideInterval(ia)) {
        final LineStringLocation bLoc = bPtLocator.locate(aCoords.getPoint(ia));
        final Point bPt = bLoc.getCoordinate();
        findNearestOnA(bPt);
      }
    }

    return this.maxInterval;
  }

  private boolean isOutsideInterval(final int ia) {
    if (ia <= this.maxInterval[0].getSegmentIndex()) {
      return true;
    }
    if (ia > this.maxInterval[1].getSegmentIndex()) {
      return true;
    }
    return false;
  }
}

package com.revolsys.gis.model.geometry.operation.noding.snapround;

import java.util.Collection;
import java.util.Iterator;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.revolsys.jts.geom.CoordinatesList;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Throws an appropriate exception if an noding error is found.
 *
 * @version 1.7
 */
public class NodingValidator {

  private final LineIntersector li = new RobustLineIntersector();

  private final Collection segStrings;

  public NodingValidator(final Collection segStrings) {
    this.segStrings = segStrings;
  }

  private void checkCollapse(final Coordinates p0, final Coordinates p1,
    final Coordinates p2) {
    if (p0.equals(p2)) {
      throw new RuntimeException("found non-noded collapse at " + p0 + " " + p1
        + " " + p2);
    }
  }

  /**
   * Checks if a segment string contains a segment pattern a-b-a (which implies a self-intersection)
   */
  private void checkCollapses() {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      checkCollapses(ss);
    }
  }

  private void checkCollapses(final SegmentString ss) {
    final CoordinatesList pts = ss.getCoordinates();
    for (int i = 0; i < pts.size() - 2; i++) {
      checkCollapse(pts.get(i), pts.get(i + 1), pts.get(i + 2));
    }
  }

  /**
   * Checks for intersections between an endpoint of a segment string
   * and an interior vertex of another segment string
   */
  private void checkEndPtVertexIntersections() {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      final CoordinatesList pts = ss.getCoordinates();
      checkEndPtVertexIntersections(pts.get(0), segStrings);
      checkEndPtVertexIntersections(pts.get(pts.size() - 1), segStrings);
    }
  }

  private void checkEndPtVertexIntersections(final Coordinates testPt,
    final Collection segStrings) {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      final CoordinatesList pts = ss.getCoordinates();
      for (int j = 1; j < pts.size() - 1; j++) {
        if (pts.get(j).equals(testPt)) {
          throw new RuntimeException(
            "found endpt/interior pt intersection at index " + j + " :pt "
              + testPt);
        }
      }
    }
  }

  /**
   * Checks all pairs of segments for intersections at an interior point of a segment
   */
  private void checkInteriorIntersections() {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss0 = (SegmentString)i.next();
      for (final Iterator j = segStrings.iterator(); j.hasNext();) {
        final SegmentString ss1 = (SegmentString)j.next();

        checkInteriorIntersections(ss0, ss1);
      }
    }
  }

  private void checkInteriorIntersections(final SegmentString e0,
    final int segIndex0, final SegmentString e1, final int segIndex1) {
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }
    // numTests++;
    final Coordinates p00 = e0.getCoordinate(segIndex0);
    final Coordinates p01 = e0.getCoordinate(segIndex0 + 1);
    final Coordinates p10 = e1.getCoordinate(segIndex1);
    final Coordinates p11 = e1.getCoordinate(segIndex1 + 1);

    li.computeIntersection(p00, p01, p10, p11);
    if (li.hasIntersection()) {

      if (li.isProper() || hasInteriorIntersection(li, p00, p01)
        || hasInteriorIntersection(li, p10, p11)) {
        throw new RuntimeException("found non-noded intersection at " + p00
          + "-" + p01 + " and " + p10 + "-" + p11);
      }
    }
  }

  private void checkInteriorIntersections(final SegmentString ss0,
    final SegmentString ss1) {
    final CoordinatesList pts0 = ss0.getCoordinates();
    final CoordinatesList pts1 = ss1.getCoordinates();
    for (int i0 = 0; i0 < pts0.size() - 1; i0++) {
      for (int i1 = 0; i1 < pts1.size() - 1; i1++) {
        checkInteriorIntersections(ss0, i0, ss1, i1);
      }
    }
  }

  public void checkValid() {
    // MD - is this call required? Or could it be done in the Interior
    // Intersection code?
    checkEndPtVertexIntersections();
    checkInteriorIntersections();
    checkCollapses();
  }

  /**
   *@return true if there is an intersection point which is not an endpoint of the segment p0-p1
   */
  private boolean hasInteriorIntersection(final LineIntersector li,
    final Coordinates p0, final Coordinates p1) {
    for (int i = 0; i < li.getIntersectionNum(); i++) {
      final Coordinates intPt = li.getIntersection(i);
      if (!(intPt.equals(p0) || intPt.equals(p1))) {
        return true;
      }
    }
    return false;
  }

}

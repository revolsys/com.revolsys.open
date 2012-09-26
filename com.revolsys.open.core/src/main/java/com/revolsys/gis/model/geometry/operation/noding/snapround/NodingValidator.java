
package com.revolsys.gis.model.geometry.operation.noding.snapround;

import java.util.Collection;
import java.util.Iterator;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Throws an appropriate exception if an noding error is found.
 *
 * @version 1.7
 */
public class NodingValidator {

  private LineIntersector li = new RobustLineIntersector();

  private Collection segStrings;

  public NodingValidator(Collection segStrings)
  {
    this.segStrings = segStrings;
  }

  public void checkValid()
  {
  	// MD - is this call required?  Or could it be done in the Interior Intersection code?
    checkEndPtVertexIntersections();
    checkInteriorIntersections();
    checkCollapses();
  }

  /**
   * Checks if a segment string contains a segment pattern a-b-a (which implies a self-intersection)
   */
  private void checkCollapses()
  {
    for (Iterator i = segStrings.iterator(); i.hasNext(); ) {
      SegmentString ss = (SegmentString) i.next();
      checkCollapses(ss);
    }
  }

  private void checkCollapses(SegmentString ss)
  {
    CoordinatesList pts = ss.getCoordinates();
    for (int i = 0; i < pts.size() - 2; i++) {
      checkCollapse(pts.get(i), pts.get(i + 1), pts.get(i + 2));
    }
  }

  private void checkCollapse(Coordinates p0, Coordinates p1, Coordinates p2)
  {
    if (p0.equals(p2))
      throw new RuntimeException("found non-noded collapse at "
                                 + p0 + " " + p1+ " " + p2);
  }

  /**
   * Checks all pairs of segments for intersections at an interior point of a segment
   */
  private void checkInteriorIntersections()
  {
    for (Iterator i = segStrings.iterator(); i.hasNext(); ) {
      SegmentString ss0 = (SegmentString) i.next();
      for (Iterator j = segStrings.iterator(); j.hasNext(); ) {
        SegmentString ss1 = (SegmentString) j.next();

          checkInteriorIntersections(ss0, ss1);
      }
    }
  }

  private void checkInteriorIntersections(SegmentString ss0, SegmentString ss1)
  {
    CoordinatesList pts0 = ss0.getCoordinates();
    CoordinatesList pts1 = ss1.getCoordinates();
    for (int i0 = 0; i0 < pts0.size() - 1; i0++) {
      for (int i1 = 0; i1 < pts1.size() - 1; i1++) {
        checkInteriorIntersections(ss0, i0, ss1, i1);
      }
    }
  }

  private void checkInteriorIntersections(SegmentString e0, int segIndex0, SegmentString e1, int segIndex1)
  {
    if (e0 == e1 && segIndex0 == segIndex1) return;
//numTests++;
    Coordinates p00 = e0.getCoordinate(segIndex0);
    Coordinates p01 = e0.getCoordinate(segIndex0 + 1);
    Coordinates p10 = e1.getCoordinate(segIndex1);
    Coordinates p11 = e1.getCoordinate(segIndex1 + 1);

    li.computeIntersection(p00, p01, p10, p11);
    if (li.hasIntersection()) {

      if (li.isProper()
          || hasInteriorIntersection(li, p00, p01)
          || hasInteriorIntersection(li, p10, p11)) {
        throw new RuntimeException("found non-noded intersection at "
                                   + p00 + "-" + p01
                                   + " and "
                                   + p10 + "-" + p11);
      }
    }
  }
  /**
   *@return true if there is an intersection point which is not an endpoint of the segment p0-p1
   */
  private boolean hasInteriorIntersection(LineIntersector li, Coordinates p0, Coordinates p1)
  {
    for (int i = 0; i < li.getIntersectionNum(); i++) {
      Coordinates intPt = li.getIntersection(i);
      if (! (intPt.equals(p0) || intPt.equals(p1)))
          return true;
    }
    return false;
  }

  /**
   * Checks for intersections between an endpoint of a segment string
   * and an interior vertex of another segment string
   */
  private void checkEndPtVertexIntersections()
  {
    for (Iterator i = segStrings.iterator(); i.hasNext(); ) {
      SegmentString ss = (SegmentString) i.next();
      CoordinatesList pts = ss.getCoordinates();
      checkEndPtVertexIntersections(pts.get(0), segStrings);
      checkEndPtVertexIntersections(pts.get(pts.size() - 1), segStrings);
    }
  }

  private void checkEndPtVertexIntersections(Coordinates testPt, Collection segStrings)
  {
    for (Iterator i = segStrings.iterator(); i.hasNext(); ) {
      SegmentString ss = (SegmentString) i.next();
      CoordinatesList pts = ss.getCoordinates();
      for (int j = 1; j < pts.size() - 1; j++) {
        if (pts.get(j).equals(testPt))
          throw new RuntimeException("found endpt/interior pt intersection at index " + j + " :pt " + testPt);
      }
    }
  }


}

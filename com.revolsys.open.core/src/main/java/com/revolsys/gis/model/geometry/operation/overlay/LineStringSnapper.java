package com.revolsys.gis.model.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.LineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;

/**
 * Snaps the vertices and segments of a {@link LineString} to a set of target
 * snap vertices. A snap distance tolerance is used to control where snapping is
 * performed.
 * <p>
 * The implementation handles empty geometry and empty snap vertex sets.
 * 
 * @author Martin Davis
 * @version 1.7
 */
public class LineStringSnapper {
  private double snapTolerance = 0.0;

  private CoordinatesList srcPts;

  private boolean allowSnappingToSourceVertices = false;

  private boolean isClosed = false;

  /**
   * Creates a new snapper using the given points as source points to be
   * snapped.
   * 
   * @param srcPts the points to snap
   * @param snapTolerance the snap tolerance to use
   */
  public LineStringSnapper(CoordinatesList srcPts, double snapTolerance) {
    this.srcPts = srcPts;
    isClosed = isClosed(srcPts);
    this.snapTolerance = snapTolerance;
  }

  public void setAllowSnappingToSourceVertices(
    boolean allowSnappingToSourceVertices) {
    this.allowSnappingToSourceVertices = allowSnappingToSourceVertices;
  }

  private static boolean isClosed(CoordinatesList pts) {
    if (pts.size() <= 1)
      return false;
    return pts.get(0).equals2d(pts.get(pts.size() - 1));
  }

  /**
   * Snaps the vertices and segments of the source LineString to the given set
   * of snap vertices.
   * 
   * @param snapPts the vertices to snap to
   * @return a list of the snapped points
   */
  public List<Coordinates> snapTo(List<Coordinates> snapPts) {
    List<Coordinates> coordList = new ArrayList<Coordinates>(srcPts.getList());

    snapVertices(coordList, snapPts);
    snapSegments(coordList, snapPts);

    return coordList;
  }

  /**
   * Snap source vertices to vertices in the target.
   * 
   * @param srcCoords the points to snap
   * @param snapPts the points to snap to
   */
  private void snapVertices(List<Coordinates> srcCoords,
    List<Coordinates> snapPts) {
    // try snapping vertices
    // if src is a ring then don't snap final vertex
    int end = isClosed ? srcCoords.size() - 1 : srcCoords.size();
    for (int i = 0; i < end; i++) {
      Coordinates srcPt = srcCoords.get(i);
      Coordinates snapVert = findSnapForVertex(srcPt, snapPts);
      if (snapVert != null) {
        // update src with snap pt
        srcCoords.set(i, new DoubleCoordinates(snapVert));
        // keep final closing point in synch (rings only)
        if (i == 0 && isClosed)
          srcCoords.set(srcCoords.size() - 1, new DoubleCoordinates(snapVert));
      }
    }
  }

  private Coordinates findSnapForVertex(Coordinates pt,
    List<Coordinates> snapPts) {
    for (int i = 0; i < snapPts.size(); i++) {
      // if point is already equal to a src pt, don't snap
      Coordinates snapPoint = snapPts.get(i);
      if (pt.equals2d(snapPoint)) {
        return null;
      } else if (pt.distance(snapPoint) < snapTolerance) {
        return snapPoint;
      }
    }
    return null;
  }

  /**
   * Snap segments of the source to nearby snap vertices. Source segments are
   * "cracked" at a snap vertex. A single input segment may be snapped several
   * times to different snap vertices.
   * <p>
   * For each distinct snap vertex, at most one source segment is snapped to.
   * This prevents "cracking" multiple segments at the same point, which would
   * likely cause topology collapse when being used on polygonal linework.
   * 
   * @param srcCoords the coordinates of the source linestring to be snapped
   * @param snapPts the target snap vertices
   */
  private void snapSegments(List<Coordinates> srcCoords,
    List<Coordinates> snapPts) {
    // guard against empty input
    if (snapPts.size() == 0)
      return;

    int distinctPtCount = snapPts.size();

    // check for duplicate snap pts when they are sourced from a linear ring.
    // TODO: Need to do this better - need to check *all* snap points for dups
    // (using a Set?)
    if (snapPts.get(0).equals2d(snapPts.get(snapPts.size() - 1)))
      distinctPtCount = snapPts.size() - 1;

    for (int i = 0; i < distinctPtCount; i++) {
      Coordinates snapPt = snapPts.get(i);
      int index = findSegmentIndexToSnap(snapPt, srcCoords);
      /**
       * If a segment to snap to was found, "crack" it at the snap pt. The new
       * pt is inserted immediately into the src segment list, so that
       * subsequent snapping will take place on the modified segments. Duplicate
       * points are not added.
       */
      if (index >= 0) {
        add(srcCoords, index + 1, new DoubleCoordinates(snapPt), false);
      }
    }
  }

  public void add(List<Coordinates> list, int i, Coordinates coord,
    boolean allowRepeated) {
    // don't add duplicate coordinates
    if (!allowRepeated) {
      int size = list.size();
      if (size > 0) {
        if (i > 0) {
          Coordinates prev = list.get(i - 1);
          if (prev.equals2d(coord))
            return;
        }
        if (i < size) {
          Coordinates next = list.get(i);
          if (next.equals2d(coord))
            return;
        }
      }
    }
    list.add(i, coord);
  }

  /**
   * Finds a src segment which snaps to (is close to) the given snap point.
   * <p>
   * Only a single segment is selected for snapping. This prevents multiple
   * segments snapping to the same snap vertex, which would almost certainly
   * cause invalid geometry to be created. (The heuristic approach to snapping
   * used here is really only appropriate when snap pts snap to a unique spot on
   * the src geometry.)
   * <p>
   * Also, if the snap vertex occurs as a vertex in the src coordinate list, no
   * snapping is performed.
   * 
   * @param snapPt the point to snap to
   * @param srcCoords the source segment coordinates
   * @return the index of the snapped segment
   * @return -1 if no segment snaps to the snap point
   */
  private int findSegmentIndexToSnap(Coordinates snapPt,
    List<Coordinates> srcCoords) {
    double minDist = Double.MAX_VALUE;
    int snapIndex = -1;
    for (int i = 0; i < srcCoords.size() - 1; i++) {
      Coordinates p0 = srcCoords.get(i);
      Coordinates p1 = srcCoords.get(i + 1);

      /**
       * Check if the snap pt is equal to one of the segment endpoints. If the
       * snap pt is already in the src list, don't snap at all.
       */
      if (p0.equals2d(snapPt) || p1.equals2d(snapPt)) {
        if (allowSnappingToSourceVertices)
          continue;
        else
          return -1;
      }

      double dist = LineSegmentUtil.distance(p0, p1, snapPt);
      if (dist < snapTolerance && dist < minDist) {
        minDist = dist;
        snapIndex = i;
      }
    }
    return snapIndex;
  }

}

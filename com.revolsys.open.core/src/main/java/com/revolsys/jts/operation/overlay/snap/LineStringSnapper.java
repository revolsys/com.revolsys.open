/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.jts.operation.overlay.snap;

import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.LineString;

/**
 * Snaps the vertices and segments of a {@link LineString} 
 * to a set of target snap vertices.
 * A snap distance tolerance is used to control where snapping is performed.
 * <p>
 * The implementation handles empty geometry and empty snap vertex sets.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class LineStringSnapper {
  public static boolean isClosed(final CoordinatesList pts) {
    if (pts.size() <= 1) {
      return false;
    }
    return pts.get(0).equals2d(pts.get(pts.size() - 1));
  }

  private double snapTolerance = 0.0;

  private final CoordinatesList srcPts;

  private boolean allowSnappingToSourceVertices = false;

  private boolean isClosed = false;

  /**
   * Creates a new snapper using the given points
   * as source points to be snapped.
   * 
   * @param srcPts the points to snap 
   * @param snapTolerance the snap tolerance to use
   */
  public LineStringSnapper(final CoordinatesList srcPts,
    final double snapTolerance) {
    this.srcPts = srcPts;
    isClosed = isClosed(srcPts);
    this.snapTolerance = snapTolerance;
  }

  /**
   * Creates a new snapper using the points in the given {@link LineString}
   * as source snap points.
   * 
   * @param srcLine a LineString to snap (may be empty)
   * @param snapTolerance the snap tolerance to use
   */
  public LineStringSnapper(final LineString srcLine, final double snapTolerance) {
    this(srcLine.getCoordinatesList(), snapTolerance);
  }

  /**
   * Finds a src segment which snaps to (is close to) the given snap point.
   * <p>
   * Only a single segment is selected for snapping.
   * This prevents multiple segments snapping to the same snap vertex,
   * which would almost certainly cause invalid geometry
   * to be created.
   * (The heuristic approach to snapping used here
   * is really only appropriate when
   * snap pts snap to a unique spot on the src geometry.)
   * <p>
   * Also, if the snap vertex occurs as a vertex in the src coordinate list,
   * no snapping is performed.
   * 
   * @param snapPt the point to snap to
   * @param srcCoords the source segment coordinates
   * @return the index of the snapped segment
   * or -1 if no segment snaps to the snap point
   */
  private int findSegmentIndexToSnap(final Coordinates snapPt,
    final CoordinateList srcCoords) {
    double minDist = Double.MAX_VALUE;
    int snapIndex = -1;
    for (int i = 0; i < srcCoords.size() - 1; i++) {
      final Coordinates p0 = srcCoords.get(i);
      final Coordinates p1 = srcCoords.get(i + 1);

      /**
       * Check if the snap pt is equal to one of the segment endpoints.
       * 
       * If the snap pt is already in the src list, don't snap at all.
       */
      if (p0.equals2d(snapPt) || p1.equals2d(snapPt)) {
        if (allowSnappingToSourceVertices) {
          continue;
        } else {
          return -1;
        }
      }

      final double dist = LineSegmentUtil.distance(p0, p1, snapPt);
      if (dist < snapTolerance && dist < minDist) {
        minDist = dist;
        snapIndex = i;
      }
    }
    return snapIndex;
  }

  private Coordinates findSnapForVertex(final Coordinates pt,
    final Coordinates[] snapPts) {
    for (int i = 0; i < snapPts.length; i++) {
      // if point is already equal to a src pt, don't snap
      if (pt.equals2d(snapPts[i])) {
        return null;
      }
      if (pt.distance(snapPts[i]) < snapTolerance) {
        return snapPts[i];
      }
    }
    return null;
  }

  public void setAllowSnappingToSourceVertices(
    final boolean allowSnappingToSourceVertices) {
    this.allowSnappingToSourceVertices = allowSnappingToSourceVertices;
  }

  /**
   * Snap segments of the source to nearby snap vertices.
   * Source segments are "cracked" at a snap vertex.
   * A single input segment may be snapped several times 
   * to different snap vertices.
   * <p>
   * For each distinct snap vertex, at most one source segment
   * is snapped to.  This prevents "cracking" multiple segments 
   * at the same point, which would likely cause 
   * topology collapse when being used on polygonal linework.
   * 
   * @param srcCoords the coordinates of the source linestring to be snapped
   * @param snapPts the target snap vertices
   */
  private void snapSegments(final CoordinateList srcCoords,
    final Coordinates[] snapPts) {
    // guard against empty input
    if (snapPts.length == 0) {
      return;
    }

    int distinctPtCount = snapPts.length;

    // check for duplicate snap pts when they are sourced from a linear ring.
    // TODO: Need to do this better - need to check *all* snap points for dups
    // (using a Set?)
    if (snapPts[0].equals2d(snapPts[snapPts.length - 1])) {
      distinctPtCount = snapPts.length - 1;
    }

    for (int i = 0; i < distinctPtCount; i++) {
      final Coordinates snapPt = snapPts[i];
      final int index = findSegmentIndexToSnap(snapPt, srcCoords);
      /**
       * If a segment to snap to was found, "crack" it at the snap pt.
       * The new pt is inserted immediately into the src segment list,
       * so that subsequent snapping will take place on the modified segments.
       * Duplicate points are not added.
       */
      if (index >= 0) {
        srcCoords.add(index + 1, new Coordinate(snapPt), false);
      }
    }
  }

  /**
   * Snaps the vertices and segments of the source LineString 
   * to the given set of snap vertices.
   * 
   * @param snapPts the vertices to snap to
   * @return a list of the snapped points
   */
  public Coordinates[] snapTo(final Coordinates[] snapPts) {
    final CoordinateList coordList = new CoordinateList(srcPts);

    snapVertices(coordList, snapPts);
    snapSegments(coordList, snapPts);

    final Coordinates[] newPts = coordList.toCoordinateArray();
    return newPts;
  }

  /**
   * Snap source vertices to vertices in the target.
   * 
   * @param srcCoords the points to snap
   * @param snapPts the points to snap to
   */
  private void snapVertices(final CoordinateList srcCoords,
    final Coordinates[] snapPts) {
    // try snapping vertices
    // if src is a ring then don't snap final vertex
    final int end = isClosed ? srcCoords.size() - 1 : srcCoords.size();
    for (int i = 0; i < end; i++) {
      final Coordinates srcPt = srcCoords.get(i);
      final Coordinates snapVert = findSnapForVertex(srcPt, snapPts);
      if (snapVert != null) {
        // update src with snap pt
        srcCoords.set(i, new Coordinate(snapVert));
        // keep final closing point in synch (rings only)
        if (i == 0 && isClosed) {
          srcCoords.set(srcCoords.size() - 1, new Coordinate(snapVert));
        }
      }
    }
  }

}

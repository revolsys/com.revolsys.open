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
package com.revolsys.gis.model.geometry.operation.geomgraph;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Allows comparing {@link Coordinates} arrays in an orientation-independent
 * way.
 * 
 * @author Martin Davis
 * @version 1.7
 */
public class OrientedCoordinateArray implements Comparable {
  private static int compareOriented(final CoordinatesList pts1,
    final boolean orientation1, final CoordinatesList pts2,
    final boolean orientation2) {
    final int dir1 = orientation1 ? 1 : -1;
    final int dir2 = orientation2 ? 1 : -1;
    final int limit1 = orientation1 ? pts1.size() : -1;
    final int limit2 = orientation2 ? pts2.size() : -1;

    int i1 = orientation1 ? 0 : pts1.size() - 1;
    int i2 = orientation2 ? 0 : pts2.size() - 1;
    final int comp = 0;
    while (true) {
      final int compPt = pts1.get(i1).compareTo(pts2.get(i2));
      if (compPt != 0) {
        return compPt;
      }
      i1 += dir1;
      i2 += dir2;
      final boolean done1 = i1 == limit1;
      final boolean done2 = i2 == limit2;
      if (done1 && !done2) {
        return -1;
      }
      if (!done1 && done2) {
        return 1;
      }
      if (done1 && done2) {
        return 0;
      }
    }
  }

  /**
   * Computes the canonical orientation for a coordinate array.
   * 
   * @param pts the array to test
   * @return <code>true</code> if the points are oriented forwards
   * @return <code>false</code if the points are oriented in reverse
   */
  private static boolean orientation(final CoordinatesList pts) {
    return CoordinateArrays.increasingDirection(pts) == 1;
  }

  private final CoordinatesList pts;

  private final boolean orientation;

  /**
   * Creates a new {@link OrientedCoordinateArray} for the given
   * {@link Coordinate} array.
   * 
   * @param pts the coordinates to orient
   */
  public OrientedCoordinateArray(final CoordinatesList pts) {
    this.pts = pts;
    orientation = orientation(pts);
  }

  /**
   * Compares two {@link OrientedCoordinateArray}s for their relative order
   * 
   * @return -1 this one is smaller
   * @return 0 the two objects are equal
   * @return 1 this one is greater
   */

  @Override
  public int compareTo(final Object o1) {
    final OrientedCoordinateArray oca = (OrientedCoordinateArray)o1;
    final int comp = compareOriented(pts, orientation, oca.pts, oca.orientation);
    /*
     * // MD - testing only int oldComp =
     * SegmentStringDissolver.ptsComp.compare(pts, oca.pts); if ((oldComp == 0
     * || comp == 0) && oldComp != comp) { System.out.println("bidir mismatch");
     * boolean orient1 = orientation(pts); boolean orient2 =
     * orientation(oca.pts); int comp2 = compareOriented(pts, orientation,
     * oca.pts, oca.orientation); int oldComp2 =
     * SegmentStringDissolver.ptsComp.compare(pts, oca.pts); }
     */
    return comp;
  }

}

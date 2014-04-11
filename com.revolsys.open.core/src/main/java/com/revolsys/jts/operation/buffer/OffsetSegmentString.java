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
package com.revolsys.jts.operation.buffer;

import java.util.ArrayList;

import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.PrecisionModel;

/**
 * A dynamic list of the vertices in a constructed offset curve.
 * Automatically removes adjacent vertices
 * which are closer than a given tolerance.
 * 
 * @author Martin Davis
 *
 */
class OffsetSegmentString {
  private static final Coordinates[] COORDINATE_ARRAY_TYPE = new Coordinates[0];

  private final ArrayList ptList;

  private PrecisionModel precisionModel = null;

  /**
   * The distance below which two adjacent points on the curve 
   * are considered to be coincident.
   * This is chosen to be a small fraction of the offset distance.
   */
  private double minimimVertexDistance = 0.0;

  public OffsetSegmentString() {
    ptList = new ArrayList();
  }

  public void addPt(final Coordinates pt) {
    final Coordinates bufPt = new Coordinate(pt);
    precisionModel.makePrecise(bufPt);
    // don't add duplicate (or near-duplicate) points
    if (isRedundant(bufPt)) {
      return;
    }
    ptList.add(bufPt);
    // System.out.println(bufPt);
  }

  public void addPts(final Coordinates[] pt, final boolean isForward) {
    if (isForward) {
      for (int i = 0; i < pt.length; i++) {
        addPt(pt[i]);
      }
    } else {
      for (int i = pt.length - 1; i >= 0; i--) {
        addPt(pt[i]);
      }
    }
  }

  public void closeRing() {
    if (ptList.size() < 1) {
      return;
    }
    final Coordinates startPt = new Coordinate((Coordinates)ptList.get(0));
    final Coordinates lastPt = (Coordinate)ptList.get(ptList.size() - 1);
    Coordinates last2Pt = null;
    if (ptList.size() >= 2) {
      last2Pt = (Coordinates)ptList.get(ptList.size() - 2);
    }
    if (startPt.equals(lastPt)) {
      return;
    }
    ptList.add(startPt);
  }

  public Coordinates[] getCoordinates() {
    /*
     * // check that points are a ring - add the startpoint again if they are
     * not if (ptList.size() > 1) { Coordinates start = (Coordinate)
     * ptList.get(0); Coordinates end = (Coordinate) ptList.get(ptList.size() -
     * 1); if (! start.equals(end) ) addPt(start); }
     */
    final Coordinates[] coord = (Coordinates[])ptList.toArray(COORDINATE_ARRAY_TYPE);
    return coord;
  }

  /**
   * Tests whether the given point is redundant
   * relative to the previous
   * point in the list (up to tolerance).
   * 
   * @param pt
   * @return true if the point is redundant
   */
  private boolean isRedundant(final Coordinates pt) {
    if (ptList.size() < 1) {
      return false;
    }
    final Coordinates lastPt = (Coordinates)ptList.get(ptList.size() - 1);
    final double ptDist = pt.distance(lastPt);
    if (ptDist < minimimVertexDistance) {
      return true;
    }
    return false;
  }

  public void reverse() {

  }

  public void setMinimumVertexDistance(final double minimimVertexDistance) {
    this.minimimVertexDistance = minimimVertexDistance;
  }

  public void setPrecisionModel(final PrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  @Override
  public String toString() {
    final GeometryFactory fact = GeometryFactory.getFactory();
    final LineString line = fact.createLineString(getCoordinates());
    return line.toString();
  }
}

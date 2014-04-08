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
package com.revolsys.gis.model.geometry.operation.buffer;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
import com.revolsys.gis.model.geometry.GeometryFactoryI;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.impl.GeometryFactoryImpl;

/**
 * A dynamic list of the vertices in a constructed offset curve. Automatically
 * removes adjacent vertices which are closer than a given tolerance.
 * 
 * @author Martin Davis
 */
class OffsetSegmentString {
  private static final Coordinates[] COORDINATE_ARRAY_TYPE = new Coordinates[0];

  private final ListCoordinatesList points = new ListCoordinatesList(3);

  private CoordinatesPrecisionModel precisionModel = null;

  /**
   * The distance below which two adjacent points on the curve are considered to
   * be coincident. This is chosen to be a small fraction of the offset
   * distance.
   */
  private double minimimVertexDistance = 0.0;

  public OffsetSegmentString() {
  }

  public void addPt(final Coordinates pt) {
    final Coordinates bufPt = new DoubleCoordinates(pt);
    precisionModel.makePrecise(bufPt);
    // don't add duplicate (or near-duplicate) points
    if (isRedundant(bufPt)) {
      return;
    }
    points.add(bufPt);
    // System.out.println(bufPt);
  }

  public void addPts(final CoordinatesList pt, final boolean isForward) {
    if (isForward) {
      for (int i = 0; i < pt.size(); i++) {
        addPt(pt.get(i));
      }
    } else {
      for (int i = pt.size() - 1; i >= 0; i--) {
        addPt(pt.get(i));
      }
    }
  }

  public void closeRing() {
    if (points.size() < 1) {
      return;
    }
    final Coordinates startPt = new DoubleCoordinates(points.get(0));
    final Coordinates lastPt = points.get(points.size() - 1);
    Coordinates last2Pt = null;
    if (points.size() >= 2) {
      last2Pt = points.get(points.size() - 2);
    }
    if (startPt.equals(lastPt)) {
      return;
    }
    points.add(startPt);
  }

  public CoordinatesList getCoordinates() {
    return points;
  }

  /**
   * Tests whether the given point is redundant relative to the previous point
   * in the list (up to tolerance).
   * 
   * @param pt
   * @return true if the point is redundant
   */
  private boolean isRedundant(final Coordinates pt) {
    if (points.size() < 1) {
      return false;
    }
    final Coordinates lastPt = points.get(points.size() - 1);
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

  public void setPrecisionModel(final CoordinatesPrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  @Override
  public String toString() {
    final GeometryFactoryI fact = GeometryFactoryImpl.getFactory();
    final LineString line = fact.createLineString(getCoordinates());
    return line.toString();
  }
}

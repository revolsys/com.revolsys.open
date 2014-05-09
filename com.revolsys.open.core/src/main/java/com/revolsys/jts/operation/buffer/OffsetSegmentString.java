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
import java.util.List;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;

/**
 * A dynamic list of the vertices in a constructed offset curve.
 * Automatically removes adjacent vertices
 * which are closer than a given tolerance.
 * 
 * @author Martin Davis
 *
 */
class OffsetSegmentString {

  private final List<Coordinates> points = new ArrayList<>();

  private GeometryFactory precisionModel = null;

  /**
   * The distance below which two adjacent points on the curve 
   * are considered to be coincident.
   * This is chosen to be a small fraction of the offset distance.
   */
  private double minimimVertexDistance = 0.0;

  public void addPt(final Coordinates point) {
    addPt(point.getX(), point.getY());
  }

  public void addPt(final double... coordinates) {
    if (!precisionModel.isFloating()) {
      coordinates[0] = precisionModel.makePrecise(0, coordinates[0]);
      coordinates[1] = precisionModel.makePrecise(1, coordinates[1]);
    }
    final Coordinates bufPt = new DoubleCoordinates(coordinates);
    if (!isRedundant(bufPt)) {
      points.add(bufPt);
    }
  }

  public void addPts(final CoordinatesList points, final boolean isForward) {
    if (isForward) {
      for (int i = 0; i < points.size(); i++) {
        addPt(points.get(i));
      }
    } else {
      for (int i = points.size() - 1; i >= 0; i--) {
        addPt(points.get(i));
      }
    }
  }

  public void closeRing() {
    if (points.size() < 1) {
      return;
    }
    final Coordinates startPt = new Coordinate(points.get(0));
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

  public CoordinatesList getPoints() {
    return new DoubleCoordinatesList(2, points);
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
    if (points.size() < 1) {
      return false;
    }
    // return points.get(points.size() - 1).equals(pt);
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

  public void setPrecisionModel(final GeometryFactory precisionModel) {
    this.precisionModel = precisionModel;
  }

  @Override
  public String toString() {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory();
    final CoordinatesList points = getPoints();
    if (points.size() == 1) {
      return geometryFactory.point(points).toString();
    } else {
      final LineString line = geometryFactory.lineString(points);
      return line.toString();
    }
  }
}

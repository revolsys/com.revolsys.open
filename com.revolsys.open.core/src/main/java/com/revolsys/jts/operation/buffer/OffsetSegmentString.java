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

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.impl.PointDouble;

/**
 * A dynamic list of the vertices in a constructed offset curve.
 * Automatically removes adjacent vertices
 * which are closer than a given tolerance.
 * 
 * @author Martin Davis
 *
 */
class OffsetSegmentString {

  private final List<Point> points = new ArrayList<>();

  private GeometryFactory precisionModel = null;

  /**
   * The distance below which two adjacent points on the curve 
   * are considered to be coincident.
   * This is chosen to be a small fraction of the offset distance.
   */
  private double minimimVertexDistance = 0.0;

  public void addPt(final double... coordinates) {
    if (!precisionModel.isFloating()) {
      coordinates[0] = precisionModel.makePrecise(0, coordinates[0]);
      coordinates[1] = precisionModel.makePrecise(1, coordinates[1]);
    }
    final Point bufPt = new PointDouble(coordinates);
    if (!isRedundant(bufPt)) {
      points.add(bufPt);
    }
  }

  public void addPt(final Point point) {
    addPt(point.getX(), point.getY());
  }

  public void addPts(final PointList points, final boolean isForward) {
    if (isForward) {
      for (int i = 0; i < points.getVertexCount(); i++) {
        addPt(points.getPoint(i));
      }
    } else {
      for (int i = points.getVertexCount() - 1; i >= 0; i--) {
        addPt(points.getPoint(i));
      }
    }
  }

  public void closeRing() {
    if (points.size() < 1) {
      return;
    }
    final Point startPt = new PointDouble(points.get(0));
    final Point lastPt = points.get(points.size() - 1);
    Point last2Pt = null;
    if (points.size() >= 2) {
      last2Pt = points.get(points.size() - 2);
    }
    if (startPt.equals(lastPt)) {
      return;
    }
    points.add(startPt);
  }

  public PointList getPoints() {
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
  private boolean isRedundant(final Point pt) {
    if (points.size() < 1) {
      return false;
    }
    // return points.get(points.size() - 1).equals(pt);
    final Point lastPt = points.get(points.size() - 1);
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
    final GeometryFactory geometryFactory = GeometryFactory.floating3();
    if (points.size() == 1) {
      return points.get(0).toString();
    } else {
      final LineString line = geometryFactory.lineString(points);
      return line.toString();
    }
  }
}

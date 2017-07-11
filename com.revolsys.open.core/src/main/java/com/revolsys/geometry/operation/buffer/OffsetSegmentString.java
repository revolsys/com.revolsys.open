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
package com.revolsys.geometry.operation.buffer;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.PointDouble;

/**
 * A dynamic list of the vertices in a constructed offset curve.
 * Automatically removes adjacent vertices
 * which are closer than a given tolerance.
 *
 * @author Martin Davis
 *
 */
class OffsetSegmentString {

  /**
   * The distance below which two adjacent points on the curve
   * are considered to be coincident.
   * This is chosen to be a small fraction of the offset distance.
   */
  private double minimimVertexDistance = 0.0;

  private final List<Point> points = new ArrayList<>();

  private GeometryFactory precisionModel = null;

  public void addPt(final double... coordinates) {
    if (!this.precisionModel.isFloating()) {
      coordinates[0] = this.precisionModel.makePrecise(0, coordinates[0]);
      coordinates[1] = this.precisionModel.makePrecise(1, coordinates[1]);
    }
    final Point bufPt = new PointDouble(coordinates);
    if (!isRedundant(bufPt)) {
      this.points.add(bufPt);
    }
  }

  public void addPt(final Point point) {
    addPt(point.getX(), point.getY());
  }

  public void addPts(final LineString points, final boolean isForward) {
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
    if (this.points.size() < 1) {
      return;
    }
    final Point startPt = new PointDouble(this.points.get(0));
    final Point lastPt = this.points.get(this.points.size() - 1);
    Point last2Pt = null;
    if (this.points.size() >= 2) {
      last2Pt = this.points.get(this.points.size() - 2);
    }
    if (startPt.equals(lastPt)) {
      return;
    }
    this.points.add(startPt);
  }

  public LineString getPoints() {
    return new LineStringDouble(2, this.points);
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
    if (this.points.size() < 1) {
      return false;
    }
    // return points.get(points.size() - 1).equals(pt);
    final Point lastPt = this.points.get(this.points.size() - 1);
    final double ptDist = pt.distance(lastPt);
    if (ptDist < this.minimimVertexDistance) {
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
    final GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;
    if (this.points.size() == 1) {
      return this.points.get(0).toString();
    } else {
      final LineString line = geometryFactory.lineString(this.points);
      return line.toString();
    }
  }
}

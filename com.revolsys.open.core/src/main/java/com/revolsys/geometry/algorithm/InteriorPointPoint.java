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
package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;

/**
 * Computes a point in the interior of an point geometry.
 * <h2>Algorithm</h2>
 * Find a point which is closest to the centroid of the geometry.
 *
 * @version 1.7
 */
public class InteriorPointPoint {

  private final Point centroid;

  private Point interiorPoint = null;

  private double minDistance = Double.MAX_VALUE;

  public InteriorPointPoint(final Geometry g) {
    this.centroid = g.getCentroid().getPoint();
    add(g);
  }

  /**
   * Tests the point(s) defined by a Geometry for the best inside point.
   * If a Geometry is not of dimension 0 it is not tested.
   * @param geom the geometry to add
   */
  private void add(final Geometry geom) {
    if (geom instanceof Point) {
      add(geom.getPoint());
    } else if (geom.isGeometryCollection()) {
      for (final Geometry part : geom.geometries()) {
        add(part);
      }
    }
  }

  private void add(final Point point) {
    final double dist = point.distancePoint(this.centroid);
    if (dist < this.minDistance) {
      this.interiorPoint = new PointDouble(point);
      this.minDistance = dist;
    }
  }

  public Point getInteriorPoint() {
    return this.interiorPoint;
  }
}

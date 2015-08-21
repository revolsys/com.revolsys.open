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
package com.revolsys.geometry.model.prep;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.AbstractPoint;

public class PreparedPoint extends AbstractPoint {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final Point point;

  public PreparedPoint(final Point point) {
    this.point = point;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.point.getBoundingBox();
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    return this.point.getCoordinate(axisIndex);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.point.getGeometryFactory();
  }

  /**
   * Tests whether this point intersects a {@link Geometry}.
   * <p>
   * The optimization here is that computing topology for the test geometry
   * is avoided.  This can be significant for large geometries.
   */
  @Override
  public boolean intersects(final Geometry geometry) {
    if (envelopesIntersect(geometry)) {
      /**
       * This avoids computing topology for the test geometry
       */
      return isAnyTargetComponentInTest(geometry);
    } else {
      return false;
    }
  }

  @Override
  public boolean isEmpty() {
    return this.point.isEmpty();
  }
}

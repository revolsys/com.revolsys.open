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

package com.revolsys.jts.shape.random;

import com.revolsys.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.jts.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.shape.GeometricShapeBuilder;

/**
 * Creates random point sets contained in a 
 * region defined by either a rectangular or a polygonal extent. 
 * 
 * @author mbdavis
 *
 */
public class RandomPointsBuilder extends GeometricShapeBuilder {
  protected Geometry maskPoly = null;

  private PointOnGeometryLocator extentLocator;

  /**
   * Create a shape factory which will create shapes using the default
   * {@link GeometryFactory}.
   */
  public RandomPointsBuilder() {
    super(GeometryFactory.getFactory());
  }

  /**
   * Create a shape factory which will create shapes using the given
   * {@link GeometryFactory}.
   *
   * @param geomFact the factory to use
   */
  public RandomPointsBuilder(final GeometryFactory geomFact) {
    super(geomFact);
  }

  @Override
  protected Point createCoord(final double x, final double y) {
    return new PointDouble(geometryFactory.makePrecise(0, x),
      geometryFactory.makePrecise(1, y));
  }

  protected Point createRandomCoord(final BoundingBox env) {
    final double x = env.getMinX() + env.getWidth() * Math.random();
    final double y = env.getMinY() + env.getHeight() * Math.random();
    return createCoord(x, y);
  }

  @Override
  public Geometry getGeometry() {
    final Point[] pts = new Point[numPts];
    int i = 0;
    while (i < numPts) {
      final Point p = createRandomCoord(getExtent());
      if (extentLocator != null && !isInExtent(p)) {
        continue;
      }
      pts[i++] = p;
    }
    return geometryFactory.multiPoint(pts);
  }

  protected boolean isInExtent(final Point p) {
    if (extentLocator != null) {
      return extentLocator.locate(p) != Location.EXTERIOR;
    }
    return getExtent().covers(p);
  }

  /**
   * Sets a polygonal mask.
   * 
   * @param mask
   * @throws IllegalArgumentException if the mask is not polygonal
   */
  public void setExtent(final Geometry mask) {
    if (!(mask instanceof Polygonal)) {
      throw new IllegalArgumentException("Only polygonal extents are supported");
    }
    this.maskPoly = mask;
    setExtent(mask.getBoundingBox());
    extentLocator = new IndexedPointInAreaLocator(mask);
  }

}

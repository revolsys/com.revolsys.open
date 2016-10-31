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

package com.revolsys.geometry.shape.random;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.shape.GeometricShapeBuilder;

/**
 * Creates random point sets
 * where the points are constrained to lie in the cells of a grid.
 *
 * @author mbdavis
 *
 */
public class RandomPointsInGridBuilder extends GeometricShapeBuilder {
  private static Point randomPointInCircle(final double orgX, final double orgY, final double width,
    final double height) {
    final double centreX = orgX + width / 2;
    final double centreY = orgY + height / 2;

    final double rndAng = 2 * Math.PI * Math.random();
    final double rndRadius = Math.random();
    // use square root of radius, since area is proportional to square of radius
    final double rndRadius2 = Math.sqrt(rndRadius);
    final double rndX = width / 2 * rndRadius2 * Math.cos(rndAng);
    final double rndY = height / 2 * rndRadius2 * Math.sin(rndAng);

    final double x0 = centreX + rndX;
    final double y0 = centreY + rndY;
    return new PointDoubleXY(x0, y0);
  }

  private double gutterFraction = 0;

  private boolean isConstrainedToCircle = false;

  /**
   * Construct a new builder which will create shapes using the default
   * {@link GeometryFactory}.
   */
  public RandomPointsInGridBuilder() {
    super(GeometryFactory.DEFAULT_3D);
  }

  /**
   * Construct a new builder which will create shapes using the given
   * {@link GeometryFactory}.
   *
   * @param geomFact the factory to use
   */
  public RandomPointsInGridBuilder(final GeometryFactory geomFact) {
    super(geomFact);
  }

  /**
   * Gets the {@link Punctual} containing the generated point
   *
   * @return a MultiPoint
   */
  @Override
  public Geometry getGeometry() {
    int nCells = (int)Math.sqrt(this.numPts);
    // ensure that at least numPts points are generated
    if (nCells * nCells < this.numPts) {
      nCells += 1;
    }

    final double gridDX = getExtent().getWidth() / nCells;
    final double gridDY = getExtent().getHeight() / nCells;

    final double gutterFrac = com.revolsys.util.MathUtil.clamp(this.gutterFraction, 0.0, 1.0);
    final double gutterOffsetX = gridDX * gutterFrac / 2;
    final double gutterOffsetY = gridDY * gutterFrac / 2;
    final double cellFrac = 1.0 - gutterFrac;
    final double cellDX = cellFrac * gridDX;
    final double cellDY = cellFrac * gridDY;

    final Point[] pts = new Point[nCells * nCells];
    int index = 0;
    for (int i = 0; i < nCells; i++) {
      for (int j = 0; j < nCells; j++) {
        final double orgX = getExtent().getMinX() + i * gridDX + gutterOffsetX;
        final double orgY = getExtent().getMinY() + j * gridDY + gutterOffsetY;
        pts[index++] = randomPointInCell(orgX, orgY, cellDX, cellDY);
      }
    }
    return this.geometryFactory.punctual(pts);
  }

  private Point randomPointInCell(final double orgX, final double orgY, final double xLen,
    final double yLen) {
    if (this.isConstrainedToCircle) {
      return randomPointInCircle(orgX, orgY, xLen, yLen);
    }
    return randomPointInGridCell(orgX, orgY, xLen, yLen);
  }

  private Point randomPointInGridCell(final double orgX, final double orgY, final double xLen,
    final double yLen) {
    final double x = orgX + xLen * Math.random();
    final double y = orgY + yLen * Math.random();
    return newPoint(x, y);
  }

  /**
   * Sets whether generated points are constrained to lie
   * within a circle contained within each grid cell.
   * This provides greater separation between points
   * in adjacent cells.
   * <p>
   * The default is to not be constrained to a circle.
   * @param isConstrainedToCircle
   */
  public void setConstrainedToCircle(final boolean isConstrainedToCircle) {
    this.isConstrainedToCircle = isConstrainedToCircle;
  }

  /**
   * Sets the fraction of the grid cell side which will be treated as
   * a gutter, in which no points will be created.
   * The provided value is clamped to the range [0.0, 1.0].
   *
   * @param gutterFraction
   */
  public void setGutterFraction(final double gutterFraction) {
    this.gutterFraction = gutterFraction;
  }

}

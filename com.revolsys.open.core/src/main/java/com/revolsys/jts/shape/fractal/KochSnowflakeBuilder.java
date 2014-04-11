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

package com.revolsys.jts.shape.fractal;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.math.Vector2D;
import com.revolsys.jts.shape.GeometricShapeBuilder;

public class KochSnowflakeBuilder extends GeometricShapeBuilder {
  private static final double ONE_THIRD = 1.0 / 3.0;

  /**
   * The height of an equilateral triangle of side one
   */
  private static final double HEIGHT_FACTOR = Math.sin(Math.PI / 3.0);

  private static final double THIRD_HEIGHT = HEIGHT_FACTOR / 3.0;

  private static final double TWO_THIRDS = 2.0 / 3.0;

  public static int recursionLevelForSize(final int numPts) {
    final double pow4 = numPts / 3;
    final double exp = Math.log(pow4) / Math.log(4);
    return (int)exp;
  }

  private final CoordinateList coordList = new CoordinateList();

  public KochSnowflakeBuilder(final GeometryFactory geomFactory) {
    super(geomFactory);
  }

  private void addSegment(final Coordinates p0, final Coordinates p1) {
    coordList.add(p1);
  }

  public void addSide(final int level, final Coordinates p0,
    final Coordinates p1) {
    if (level == 0) {
      addSegment(p0, p1);
    } else {
      final Vector2D base = Vector2D.create(p0, p1);
      final Coordinates midPt = base.multiply(0.5).translate(p0);

      final Vector2D heightVec = base.multiply(THIRD_HEIGHT);
      final Vector2D offsetVec = heightVec.rotateByQuarterCircle(1);
      final Coordinates offsetPt = offsetVec.translate(midPt);

      final int n2 = level - 1;
      final Coordinates thirdPt = base.multiply(ONE_THIRD).translate(p0);
      final Coordinates twoThirdPt = base.multiply(TWO_THIRDS).translate(p0);

      // construct sides recursively
      addSide(n2, p0, thirdPt);
      addSide(n2, thirdPt, offsetPt);
      addSide(n2, offsetPt, twoThirdPt);
      addSide(n2, twoThirdPt, p1);
    }
  }

  private Coordinates[] getBoundary(final int level, final Coordinates origin,
    final double width) {
    double y = origin.getY();
    // for all levels beyond 0 need to vertically shift shape by height of one
    // "arm" to centre it
    if (level > 0) {
      y += THIRD_HEIGHT * width;
    }

    final Coordinate p0 = new Coordinate(origin.getX(), y,
      Coordinates.NULL_ORDINATE);
    final Coordinate p1 = new Coordinate(origin.getX() + width / 2, y + width
      * HEIGHT_FACTOR, Coordinates.NULL_ORDINATE);
    final Coordinate p2 = new Coordinate(origin.getX() + width, y,
      Coordinates.NULL_ORDINATE);
    addSide(level, p0, p1);
    addSide(level, p1, p2);
    addSide(level, p2, p0);
    coordList.closeRing();
    return coordList.toCoordinateArray();
  }

  @Override
  public Geometry getGeometry() {
    final int level = recursionLevelForSize(numPts);
    final LineSegment baseLine = getSquareBaseLine();
    final Coordinates[] pts = getBoundary(level, baseLine.getCoordinate(0),
      baseLine.getLength());
    return geomFactory.createPolygon(geomFactory.createLinearRing(pts), null);
  }

}

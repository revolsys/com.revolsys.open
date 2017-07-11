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

package com.revolsys.geometry.shape.fractal;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.CoordinateList;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.shape.GeometricShapeBuilder;

public class SierpinskiCarpetBuilder extends GeometricShapeBuilder {
  public static int recursionLevelForSize(final int numPts) {
    final double pow4 = numPts / 3;
    final double exp = Math.log(pow4) / Math.log(4);
    return (int)exp;
  }

  private final CoordinateList coordList = new CoordinateList();

  public SierpinskiCarpetBuilder(final GeometryFactory geomFactory) {
    super(geomFactory);
  }

  private void addHoles(final int n, final double originX, final double originY, final double width,
    final List<LinearRing> holeList) {
    if (n < 0) {
      return;
    }
    final int n2 = n - 1;
    final double widthThird = width / 3.0;
    addHoles(n2, originX, originY, widthThird, holeList);
    addHoles(n2, originX + widthThird, originY, widthThird, holeList);
    addHoles(n2, originX + 2 * widthThird, originY, widthThird, holeList);

    addHoles(n2, originX, originY + widthThird, widthThird, holeList);
    addHoles(n2, originX + 2 * widthThird, originY + widthThird, widthThird, holeList);

    addHoles(n2, originX, originY + 2 * widthThird, widthThird, holeList);
    addHoles(n2, originX + widthThird, originY + 2 * widthThird, widthThird, holeList);
    addHoles(n2, originX + 2 * widthThird, originY + 2 * widthThird, widthThird, holeList);

    // add the centre hole
    holeList.add(newSquareHole(originX + widthThird, originY + widthThird, widthThird));
  }

  @Override
  public Geometry getGeometry() {
    final int level = recursionLevelForSize(this.numPts);
    final LineSegment baseLine = getSquareBaseLine();
    final Point origin = baseLine.getPoint(0);
    final List<LinearRing> rings = new ArrayList<>();
    final LinearRing shell = ((Polygon)getSquareExtent().toGeometry()).getShell();
    rings.add(shell);
    addHoles(level, origin.getX(), origin.getY(), getDiameter(), rings);
    return this.geometryFactory.polygon(shell);
  }

  private LinearRing newSquareHole(final double x, final double y, final double width) {
    final Point[] pts = new Point[] {
      new PointDouble(x, y),
      new PointDouble(x + width, y),
      new PointDouble(x + width, y + width),
      new PointDouble(x, y + width),
      new PointDouble(x, y)
    };
    return this.geometryFactory.linearRing(pts);
  }

}

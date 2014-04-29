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
package com.revolsys.jts.geom;

/**
 * Represents a line segment defined by two {@link Coordinates}s.
 * Provides methods to compute various geometric properties
 * and relationships of line segments.
 * <p>
 * This class is designed to be easily mutable (to the extent of
 * having its contained points public).
 * This supports a common pattern of reusing a single LineSegmentImpl
 * object as a way of computing segment properties on the
 * segments defined by arrays or lists of {@link Coordinates}s.
 *
 *@version 1.7
 */
public class LineSegmentImpl extends AbstractLineSegment {

  private final Coordinates p0;

  private final Coordinates p1;

  public LineSegmentImpl() {
    this(new Coordinate(), new Coordinate());
  }

  public LineSegmentImpl(final Coordinates p0, final Coordinates p1) {
    this.p0 = p0;
    this.p1 = p1;
  }

  public LineSegmentImpl(final double x0, final double y0, final double x1,
    final double y1) {
    this(new Coordinate(x0, y0, Coordinates.NULL_ORDINATE), new Coordinate(x1,
      y1, Coordinates.NULL_ORDINATE));
  }

  public LineSegmentImpl(final LineSegment ls) {
    this(ls.getP0(), ls.getP1());
  }

  @Override
  public LineSegmentImpl clone() {
    return new LineSegmentImpl(get(0), get(1));
  }

  @Override
  public Coordinates getP0() {
    return p0;
  }

  @Override
  public Coordinates getP1() {
    return p1;
  }

  @Override
  public double getValue(final int vertexIndex, final int axisIndex) {
    if (vertexIndex == 0) {
      return p0.getValue(axisIndex);
    } else if (vertexIndex == 1) {
      return p1.getValue(axisIndex);
    }
    {
      return Double.NaN;
    }
  }

}

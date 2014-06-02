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

import java.util.List;

/**
 *  Models an OGC-style <code>LineString</code>.
 *  A LineString consists of a sequence of two or more vertices,
 *  along with all points along the linearly-interpolated curves
 *  (line segments) between each 
 *  pair of consecutive vertices.
 *  Consecutive vertices may be equal.
 *  The line segments in the line may intersect each other (in other words, 
 *  the linestring may "curl back" in itself and self-intersect.
 *  Linestrings with exactly two identical points are invalid. 
 *  <p> 
 * A linestring must have either 0 or 2 or more points.  
 * If these conditions are not met, the constructors throw 
 * an {@link IllegalArgumentException}
 *
 *@version 1.7
 */
public interface LineString extends Lineal {
  /**
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */

  @Override
  LineString clone();

  double distance(int index, Point point);

  boolean equals(int axisIndex, int vertexIndex, Point point);

  boolean equalsVertex(final int vertexIndex, final double... coordinates);

  boolean equalsVertex(int axisCount, final int vertexIndex1,
    final int vertexIndex2);

  boolean equalsVertex(int axisCount, final int vertexIndex,
    final LineString line2, int vertexIndex2);

  boolean equalsVertex(int axisCount, final int vertexIndex, final Point point);

  boolean equalsVertex(final int vertexIndex, final Point point);

  double getCoordinate(int vertexIndex, final int axisIndex);

  double[] getCoordinates();

  LineString getCoordinatesList();

  Point getEndPoint();

  double getM(int vertexIndex);

  Point getPoint(final int vertexIndex);

  int getSegmentCount();

  Point getStartPoint();

  double getX(int vertexIndex);

  double getY(int vertexIndex);

  double getZ(int vertexIndex);

  boolean hasVertex(Point point);

  boolean isClockwise();

  boolean isClosed();

  boolean isCounterClockwise();

  boolean isRing();

  /**
   * Merge two lines that share common coordinates at either the start or end.
   * If the lines touch only at their start coordinates, the line2 will be
   * reversed and joined before the start of line1. If the two lines touch only
   * at their end coordinates, the line2 will be reversed and joined after the
   * end of line1.
   * 
   * @param line1 The first line.
   * @param line2 The second line.
   * @return The new line string
   */
  LineString merge(LineString line);

  LineString merge(Point point, LineString line);

  @Override
  LineString move(final double... deltas);

  /**
   * Normalizes a LineString.  A normalized linestring
   * has the first point which is not equal to it's reflected point
   * less than the reflected point.
   */

  @Override
  LineString normalize();

  Iterable<Point> points();

  @Override
  LineString prepare();

  @Override
  LineString reverse();

  List<LineString> split(Point point);

  LineString subLine(final int vertexCount);

  LineString subLine(final int fromVertexIndex, int vertexCount);

  LineString subLine(final int vertexCount, final Point toPoint);

  LineString subLine(final Point fromPoint, final int fromVertexIndex,
    int vertexCount, final Point toPoint);

}

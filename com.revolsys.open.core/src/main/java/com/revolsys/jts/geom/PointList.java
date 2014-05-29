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

import java.io.Serializable;
import java.util.List;

/**
 * The internal representation of a list of coordinates inside a Geometry.
 * <p>
 * This allows Geometries to store their
 * points using something other than the JTS {@link Coordinates} class. 
 * For example, a storage-efficient implementation
 * might store coordinate sequences as an array of x's
 * and an array of y's. 
 * Or a custom coordinate class might support extra attributes like M-values.
 * <p>
 * Implementing a custom coordinate storage structure
 * requires implementing the {@link PointList} and
 *interfaces. 
 * To use the custom PointList, create a
 * new {@link GeometryFactory} parameterized by the CoordinateSequenceFactory
 * The {@link GeometryFactory} can then be used to create new {@link Geometry}s.
 * The new Geometries
 * will use the custom PointList implementation.
 * <p>
 * For an example, see the code for
 * {@link ExtendedCoordinateExample}.
 *
 *
 * @version 1.7
 */
public interface PointList extends Cloneable, Serializable {
  // done
  PointList clone();

  // done
  double distance(int index, Point point);

  // done
  boolean equalsVertex(int axisCount, int vertexIndex, Point point);

  // done
  int getAxisCount();

  // done
  double getCoordinate(int index, int axisIndex);

  // done
  double[] getCoordinates();

  // done
  double getM(int index);

  // done
  Point getPoint(int i);

  // done
  int getVertexCount();

  // done
  double getX(int index);

  // done
  double getY(int index);

  // done
  double getZ(int index);

  // done
  boolean hasVertex(Point point);

  // done
  boolean isCounterClockwise();

  // done
  PointList reverse();

  // done
  PointList subLine(int index);

  // done
  PointList subLine(int index, int count);

  List<Point> toPointList();
}

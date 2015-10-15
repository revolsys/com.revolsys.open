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

package com.revolsys.geometry.model;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.revolsys.geometry.model.impl.PointDouble2D;

public interface Polygonal extends Geometry {
  @Override
  default boolean contains(final double x, final double y) {
    return locate(new PointDouble2D(x, y)) != Location.EXTERIOR;
  }

  @Override
  default boolean contains(final double x, final double y, final double w, final double h) {
    return false;
  }

  @Override
  default boolean contains(final Point2D point) {
    final double x = point.getX();
    final double y = point.getY();
    return contains(x, y);
  }

  @Override
  default boolean contains(final Rectangle2D rectangle) {
    final double x = rectangle.getX();
    final double y = rectangle.getY();
    final double width = rectangle.getWidth();
    final double height = rectangle.getHeight();
    return contains(x, y, width, height);
  }

  Iterable<Polygon> polygons();
}

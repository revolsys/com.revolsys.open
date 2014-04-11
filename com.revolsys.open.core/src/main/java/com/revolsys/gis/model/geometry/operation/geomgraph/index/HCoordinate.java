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
package com.revolsys.gis.model.geometry.operation.geomgraph.index;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.algorithm.NotRepresentableException;
import com.revolsys.jts.geom.Coordinates;

/**
 * Represents a homogeneous coordinate in a 2-D coordinate space. In JTS
 * {@link HCoordinates}s are used as a clean way of computing intersections
 * between line segments.
 * 
 * @author David Skea
 * @version 1.7
 */
public class HCoordinate {

  /**
   * Computes the (approximate) intersection point between two line segments
   * using homogeneous coordinates.
   * <p>
   * Note that this algorithm is not numerically stable; i.e. it can produce
   * intersection points which lie outside the envelope of the line segments
   * themselves. In order to increase the precision of the calculation input
   * points should be normalized before passing them to this routine.
   */
  public static Coordinates intersection(final Coordinates p1,
    final Coordinates p2, final Coordinates q1, final Coordinates q2)
    throws NotRepresentableException {
    // unrolled computation
    final double px = p1.getY() - p2.getY();
    final double py = p2.getX() - p1.getX();
    final double pw = p1.getX() * p2.getY() - p2.getX() * p1.getY();

    final double qx = q1.getY() - q2.getY();
    final double qy = q2.getX() - q1.getX();
    final double qw = q1.getX() * q2.getY() - q2.getX() * q1.getY();

    final double x = py * qw - qy * pw;
    final double y = qx * pw - px * qw;
    final double w = px * qy - qx * py;

    final double xInt = x / w;
    final double yInt = y / w;

    if ((Double.isNaN(xInt)) || (Double.isInfinite(xInt) || Double.isNaN(yInt))
      || (Double.isInfinite(yInt))) {
      throw new NotRepresentableException();
    }

    return new DoubleCoordinates(xInt, yInt);
  }

  /*
   * public static Coordinates OLDintersection( Coordinates p1, Coordinates p2,
   * Coordinates q1, Coordinates q2) throws NotRepresentableException {
   * HCoordinates l1 = new HCoordinates(p1, p2); HCoordinates l2 = new
   * HCoordinates(q1, q2); HCoordinates intHCoord = new HCoordinates(l1, l2);
   * Coordinates intPt = intHCoord.getCoordinates(); return intPt; }
   */

  public double x, y, w;

  public HCoordinate() {
    x = 0.0;
    y = 0.0;
    w = 1.0;
  }

  public HCoordinate(final Coordinates p) {
    x = p.getX();
    y = p.getY();
    w = 1.0;
  }

  /**
   * Constructs a homogeneous coordinate which is the intersection of the lines
   * define by the homogenous coordinates represented by two {@link Coordinates}
   * s.
   * 
   * @param p1
   * @param p2
   */
  public HCoordinate(final Coordinates p1, final Coordinates p2) {
    // optimization when it is known that w = 1
    x = p1.getY() - p2.getY();
    y = p2.getX() - p1.getX();
    w = p1.getX() * p2.getY() - p2.getX() * p1.getY();
  }

  public HCoordinate(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    // unrolled computation
    final double px = p1.getY() - p2.getY();
    final double py = p2.getX() - p1.getX();
    final double pw = p1.getX() * p2.getY() - p2.getX() * p1.getY();

    final double qx = q1.getY() - q2.getY();
    final double qy = q2.getX() - q1.getX();
    final double qw = q1.getX() * q2.getY() - q2.getX() * q1.getY();

    x = py * qw - qy * pw;
    y = qx * pw - px * qw;
    w = px * qy - qx * py;
  }

  public HCoordinate(final double _x, final double _y) {
    x = _x;
    y = _y;
    w = 1.0;
  }

  public HCoordinate(final double _x, final double _y, final double _w) {
    x = _x;
    y = _y;
    w = _w;
  }

  public HCoordinate(final HCoordinate p1, final HCoordinate p2) {
    x = p1.y * p2.w - p2.y * p1.w;
    y = p2.x * p1.w - p1.x * p2.w;
    w = p1.x * p2.y - p2.x * p1.y;
  }

  public Coordinates getCoordinates() throws NotRepresentableException {
    final Coordinates p = new DoubleCoordinates(getX(), getY());
    return p;
  }

  public double getX() throws NotRepresentableException {
    final double a = x / w;
    if ((Double.isNaN(a)) || (Double.isInfinite(a))) {
      throw new NotRepresentableException();
    }
    return a;
  }

  public double getY() throws NotRepresentableException {
    final double a = y / w;
    if ((Double.isNaN(a)) || (Double.isInfinite(a))) {
      throw new NotRepresentableException();
    }
    return a;
  }
}

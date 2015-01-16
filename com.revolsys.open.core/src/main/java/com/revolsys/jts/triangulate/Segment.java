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

package com.revolsys.jts.triangulate;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDouble;

/**
 * Models a constraint segment in a triangulation.
 * A constraint segment is an oriented straight line segment between a start point
 * and an end point.
 *
 * @author David Skea
 * @author Martin Davis
 */
public class Segment {
  private final LineSegment ls;

  private Object data = null;

  /**
   * Creates a new instance for the given ordinates.
   */
  public Segment(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2) {
    this(new PointDouble(x1, y1, z1), new PointDouble(x2, y2, z2));
  }

  /**
   * Creates a new instance for the given ordinates,  with associated external data.
   */
  public Segment(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2, final Object data) {
    this(new PointDouble(x1, y1, z1), new PointDouble(x2, y2, z2), data);
  }

  /**
   * Creates a new instance for the given points.
   *
   * @param p0 the start point
   * @param p1 the end point
   */
  public Segment(final Point p0, final Point p1) {
    this.ls = new LineSegmentDouble(p0, p1);
  }

  /**
   * Creates a new instance for the given points, with associated external data.
   *
   * @param p0 the start point
   * @param p1 the end point
   * @param data an external data object
   */
  public Segment(final Point p0, final Point p1, final Object data) {
    this.ls = new LineSegmentDouble(p0, p1);
    this.data = data;
  }

  /**
   * Determines whether two segments are topologically equal.
   * I.e. equal up to orientation.
   *
   * @param s a segment
   * @return true if the segments are topologically equal
   */
  public boolean equalsTopo(final Segment s) {
    return this.ls.equalsTopo(s.getLineSegment());
  }

  /**
   * Gets the external data associated with this segment
   *
   * @return a data object
   */
  public Object getData() {
    return this.data;
  }

  /**
   * Gets the end coordinate of the segment
   *
   * @return a Coordinate
   */
  public Point getEnd() {
    return this.ls.getPoint(1);
  }

  /**
   * Gets the end X ordinate of the segment
   *
   * @return the X ordinate value
   */
  public double getEndX() {
    final Point p = this.ls.getPoint(1);
    return p.getX();
  }

  /**
   * Gets the end Y ordinate of the segment
   *
   * @return the Y ordinate value
   */
  public double getEndY() {
    final Point p = this.ls.getPoint(1);
    return p.getY();
  }

  /**
   * Gets the end Z ordinate of the segment
   *
   * @return the Z ordinate value
   */
  public double getEndZ() {
    final Point p = this.ls.getPoint(1);
    return p.getZ();
  }

  /**
   * Gets a <tt>LineSegmentDouble</tt> modelling this segment.
   *
   * @return a LineSegmentDouble
   */
  public LineSegment getLineSegment() {
    return this.ls;
  }

  /**
   * Gets the start coordinate of the segment
   *
   * @return a Coordinate
   */
  public Point getStart() {
    return this.ls.getPoint(0);
  }

  /**
   * Gets the start X ordinate of the segment
   *
   * @return the X ordinate value
   */
  public double getStartX() {
    final Point p = this.ls.getPoint(0);
    return p.getX();
  }

  /**
   * Gets the start Y ordinate of the segment
   *
   * @return the Y ordinate value
   */
  public double getStartY() {
    final Point p = this.ls.getPoint(0);
    return p.getY();
  }

  /**
   * Gets the start Z ordinate of the segment
   *
   * @return the Z ordinate value
   */
  public double getStartZ() {
    final Point p = this.ls.getPoint(0);
    return p.getZ();
  }

  /**
   * Computes the intersection point between this segment and another one.
   *
   * @param s a segment
   * @return the intersection point, or <code>null</code> if there is none
   */
  public Point intersection(final Segment s) {
    return this.ls.intersection(s.getLineSegment());
  }

  /**
   * Sets the external data to be associated with this segment
   *
   * @param data a data object
   */
  public void setData(final Object data) {
    this.data = data;
  }

  /**
   * Computes a string representation of this segment.
   *
   * @return a string
   */
  @Override
  public String toString() {
    return this.ls.toString();
  }
}

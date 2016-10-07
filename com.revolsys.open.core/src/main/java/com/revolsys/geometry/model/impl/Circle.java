/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class Circle extends PointDouble {

  private static final long serialVersionUID = 1L;

  private BoundingBox boundingBox;

  private final double radius;

  private final double tolerance = 0.0001;

  public Circle(final Point centre, final double radius) {
    super(centre);
    this.radius = radius;
    this.boundingBox = new BoundingBoxDoubleXY(getX(), getY());
    this.boundingBox = this.boundingBox.expand(radius);
  }

  public boolean contains(final Point point) {
    final double distanceFromCentre = distance(point);
    return distanceFromCentre < this.radius + this.tolerance;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public double getRadius() {
    return this.radius;
  }

  public Geometry toGeometry() {
    final GeometryFactory factory = GeometryFactory.DEFAULT;
    final Point point = factory.point(this);
    return point.buffer(this.radius);
  }

  @Override
  public String toString() {
    return "CIRCLE(" + getX() + " " + getY() + " " + this.radius + ")";
  }
}

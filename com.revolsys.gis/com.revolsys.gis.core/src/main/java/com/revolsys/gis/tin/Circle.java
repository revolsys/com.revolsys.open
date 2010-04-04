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
package com.revolsys.gis.tin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Circle {
  private final Coordinate centre;

  private final Envelope envelope;

  private final double radius;

  private final double tolerance = 0.0001;

  public Circle(
    final Coordinate centre,
    final double radius) {
    this.centre = centre;
    this.radius = radius;
    this.envelope = new Envelope(centre.x - radius, centre.x + radius, centre.y
      - radius, centre.y + radius);
  }

  public boolean contains(
    final Coordinate coordinate) {
    final double distanceFromCentre = centre.distance(coordinate);
    return distanceFromCentre < (this.radius + tolerance);
  }

  public Coordinate getCenter() {
    return centre;
  }

  public Envelope getEnvelopeInternal() {
    return envelope;
  }

  public double getRadius() {
    return radius;
  }

  public Geometry toGeometry() {
    final GeometryFactory factory = new GeometryFactory();
    final Point point = factory.createPoint(centre);
    return point.buffer(radius);
  }

  @Override
  public String toString() {
    return "CIRCLE(" + centre.x + " " + centre.y + " " + radius + ")";
  }
}

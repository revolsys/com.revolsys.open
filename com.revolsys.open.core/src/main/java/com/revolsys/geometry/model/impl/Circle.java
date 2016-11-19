package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public class Circle extends PointDoubleXY {

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
    final double distanceFromCentre = distancePoint(point);
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
    return buffer(this.radius);
  }

  @Override
  public String toString() {
    return "CIRCLE(" + getX() + " " + getY() + " " + this.radius + ")";
  }
}

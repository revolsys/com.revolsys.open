package com.revolsys.gis.tin;

import java.util.function.Predicate;

import com.revolsys.geometry.model.Point;

public class TriangleContainsPointFilter implements Predicate<Triangle> {
  private final Point point;

  public TriangleContainsPointFilter(final Point point) {
    this.point = point;
  }

  @Override
  public boolean test(final Triangle triangle) {
    return triangle.hasVertex(this.point);
  }
}

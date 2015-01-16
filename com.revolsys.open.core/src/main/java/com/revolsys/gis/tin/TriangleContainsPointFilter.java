package com.revolsys.gis.tin;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Point;

public class TriangleContainsPointFilter implements Filter<Triangle> {
  private final Point point;

  public TriangleContainsPointFilter(final Point point) {
    this.point = point;
  }

  @Override
  public boolean accept(final Triangle triangle) {
    return triangle.hasVertex(this.point);
  }
}

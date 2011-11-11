package com.revolsys.gis.model.geometry.visitor;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.Triangle;

public class TriangleContainsPointFilter implements Filter<Triangle> {
  private Coordinates point;

  public TriangleContainsPointFilter(Coordinates point) {
    this.point = point;
  }

  public boolean accept(Triangle triangle) {
    return triangle.contains(point);
  }
}

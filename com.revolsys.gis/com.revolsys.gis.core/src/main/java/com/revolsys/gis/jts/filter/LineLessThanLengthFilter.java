package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Geometry;

public class LineLessThanLengthFilter implements Filter<Geometry> {
  private double length;

  public LineLessThanLengthFilter() {
  }

  public LineLessThanLengthFilter(
    final double length) {
    this.length = length;
  }

  public boolean accept(
    final Geometry geometry) {
    return geometry.getLength() < length;
  }

  public double getLength() {
    return length;
  }

  public void setLength(
    final double length) {
    this.length = length;
  }
}

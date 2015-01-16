package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;

public class LineLessThanLengthFilter implements Filter<Geometry> {
  private double length;

  public LineLessThanLengthFilter() {
  }

  public LineLessThanLengthFilter(final double length) {
    this.length = length;
  }

  @Override
  public boolean accept(final Geometry geometry) {
    return geometry.getLength() < this.length;
  }

  public double getLength() {
    return this.length;
  }

  public void setLength(final double length) {
    this.length = length;
  }
}

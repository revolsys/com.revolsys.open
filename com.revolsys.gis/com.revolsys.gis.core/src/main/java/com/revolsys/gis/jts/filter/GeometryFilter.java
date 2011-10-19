package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.LineString;

public class GeometryFilter {
  public static Filter<LineString> lineEqualWithinTolerance(LineString line,
    double tolerance) {
    return new LineEqualWithinToleranceFilter(line, tolerance);
  }

  public static Filter<LineString> lineContainsWithinTolerance(LineString line,
    double tolerance) {
    return new LineContainsWithinToleranceFilter(line, tolerance);
  }

  public static Filter<LineString> lineContainedWithinTolerance(
    LineString line, double tolerance) {
    return new LineContainsWithinToleranceFilter(line, tolerance, true);
  }
}

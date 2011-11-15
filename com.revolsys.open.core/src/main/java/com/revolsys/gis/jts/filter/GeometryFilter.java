package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.filter.InvokeMethodFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class GeometryFilter {
  public static Filter<LineString> lineEqualWithinTolerance(LineString line,
    double maxDistance) {
    return new LineEqualWithinToleranceFilter(line, maxDistance);
  }

  public static Filter<LineString> lineContainsWithinTolerance(LineString line,
    double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance);
  }

  public static Filter<LineString> lineContainedWithinTolerance(
    LineString line, double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance, true);
  }

  public static Filter<LineString> lineWithinDistance(LineString line,
    double maxDistance) {
    return new LineStringLessThanDistanceFilter(line, maxDistance);
  }

  public static <T extends Geometry> Filter<T> intersects(Envelope envelope) {
    return new InvokeMethodFilter<T>(GeometryFilter.class,
      "acceptEnvelopeIntersects", envelope);
  }

  public static boolean acceptEnvelopeIntersects(Envelope envelope,
    Geometry geometry) {
    final Envelope geometryEnvelope = geometry.getEnvelopeInternal();
    return envelope.intersects(geometryEnvelope);
  }
}

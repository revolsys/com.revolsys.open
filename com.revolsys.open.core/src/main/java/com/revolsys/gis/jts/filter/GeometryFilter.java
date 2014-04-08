package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.filter.InvokeMethodFilter;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;

public class GeometryFilter {
  public static boolean acceptEnvelopeIntersects(final Envelope envelope,
    final Geometry geometry) {
    final Envelope geometryEnvelope = geometry.getEnvelopeInternal();
    return envelope.intersects(geometryEnvelope);
  }

  public static <T extends Geometry> Filter<T> intersects(
    final Envelope envelope) {
    return new InvokeMethodFilter<T>(GeometryFilter.class,
      "acceptEnvelopeIntersects", envelope);
  }

  public static Filter<LineString> lineContainedWithinTolerance(
    final LineString line, final double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance, true);
  }

  public static Filter<LineString> lineContainsWithinTolerance(
    final LineString line, final double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance);
  }

  public static Filter<LineString> lineEqualWithinTolerance(
    final LineString line, final double maxDistance) {
    return new LineEqualWithinToleranceFilter(line, maxDistance);
  }

  public static Filter<LineString> lineWithinDistance(final LineString line,
    final double maxDistance) {
    return new LineStringLessThanDistanceFilter(line, maxDistance);
  }
}

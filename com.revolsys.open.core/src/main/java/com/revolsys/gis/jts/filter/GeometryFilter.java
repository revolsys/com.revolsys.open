package com.revolsys.gis.jts.filter;

import java.util.function.Predicate;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.predicate.InvokeMethodPredicate;

public class GeometryFilter {
  public static boolean testEnvelopeIntersects(final BoundingBox envelope,
    final Geometry geometry) {
    final BoundingBox geometryEnvelope = geometry.getBoundingBox();
    return envelope.intersects(geometryEnvelope);
  }

  public static <T extends Geometry> Predicate<T> intersects(final BoundingBox envelope) {
    return new InvokeMethodPredicate<T>(GeometryFilter.class, "acceptEnvelopeIntersects", envelope);
  }

  public static Predicate<LineString> lineContainedWithinTolerance(final LineString line,
    final double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance, true);
  }

  public static Predicate<LineString> lineContainsWithinTolerance(final LineString line,
    final double maxDistance) {
    return new LineContainsWithinToleranceFilter(line, maxDistance);
  }

  public static Predicate<LineString> lineWithinDistance(final LineString line,
    final double maxDistance) {
    return new LineStringLessThanDistanceFilter(line, maxDistance);
  }
}

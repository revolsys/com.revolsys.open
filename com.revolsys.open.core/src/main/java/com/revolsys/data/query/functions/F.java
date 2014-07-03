package com.revolsys.data.query.functions;

import com.revolsys.data.query.Column;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.query.Value;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;

public class F {
  public static WithinDistance dWithin(final Attribute attribute,
    final Geometry geometry, final double distance) {
    return new WithinDistance(new Column(attribute), new Value(attribute,
      geometry), new Value(distance));
  }

  public static WithinDistance dWithin(final String name,
    final Geometry geometry, double distance) {
    if (distance < 0) {
      distance = 0;
    }
    return new WithinDistance(new Column(name), new Value(geometry), new Value(
      distance));
  }

  public static EnvelopeIntersects envelopeIntersects(
    final Attribute attribute, final BoundingBox boundingBox) {
    return new EnvelopeIntersects(new Column(attribute), new Value(attribute,
      boundingBox));
  }

  public static EnvelopeIntersects envelopeIntersects(
    final Attribute attribute, final Geometry geometry) {
    return new EnvelopeIntersects(new Column(attribute), new Value(attribute,
      geometry.getBoundingBox()));
  }

  public static EnvelopeIntersects envelopeIntersects(final String name,
    final BoundingBox boundingBox) {
    return new EnvelopeIntersects(new Column(name), new Value(boundingBox));
  }

  public static Lower lower(final QueryValue value) {

    return new Lower(value);
  }

  public static RegexpReplace regexpReplace(final QueryValue value,
    final String pattern, final String replace) {
    return new RegexpReplace(value, pattern, replace);
  }

  public static RegexpReplace regexpReplace(final QueryValue value,
    final String pattern, final String replace, final String flags) {
    return new RegexpReplace(value, pattern, replace, flags);
  }

  public static Upper upper(final QueryValue value) {
    return new Upper(value);
  }

  public static Upper upper(final String name) {
    return upper(new Column(name));
  }
}

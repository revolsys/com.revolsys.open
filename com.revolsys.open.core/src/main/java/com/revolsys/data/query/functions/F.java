package com.revolsys.data.query.functions;

import com.revolsys.data.query.Column;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.query.Value;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;

public class F {
  public static WithinDistance dWithin(final FieldDefinition attribute,
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
    final FieldDefinition attribute, final BoundingBox boundingBox) {
    if (attribute == null) {
      return null;
    } else {
      final Column column = new Column(attribute);
      final Value value = new Value(attribute, boundingBox);
      return new EnvelopeIntersects(column, value);
    }
  }

  public static EnvelopeIntersects envelopeIntersects(
    final FieldDefinition attribute, final Geometry geometry) {
    return new EnvelopeIntersects(new Column(attribute), new Value(attribute,
      geometry.getBoundingBox()));
  }

  public static EnvelopeIntersects envelopeIntersects(
    final RecordDefinition recordDefinition, final BoundingBox boundingBox) {
    final FieldDefinition attribute = recordDefinition.getGeometryField();
    return envelopeIntersects(attribute, boundingBox);
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

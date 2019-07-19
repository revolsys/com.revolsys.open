package com.revolsys.record.query.functions;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class F {
  public static WithinDistance dWithin(final FieldDefinition fieldDefinition,
    final Geometry geometry, final double distance) {
    final Column column = new Column(fieldDefinition);
    final Value geometryValue = new Value(fieldDefinition, geometry);
    final Value distanceValue = new Value(distance);
    return new WithinDistance(column, geometryValue, distanceValue);
  }

  public static WithinDistance dWithin(final String name, final Geometry geometry,
    double distance) {
    if (distance < 0) {
      distance = 0;
    }
    final Column column = new Column(name);
    final Value geometryValue = new Value(geometry);
    final Value distanceValue = new Value(distance);
    return new WithinDistance(column, geometryValue, distanceValue);
  }

  public static EnvelopeIntersects envelopeIntersects(final FieldDefinition attribute,
    final BoundingBox boundingBox) {
    if (attribute == null) {
      return null;
    } else {
      final Column column = new Column(attribute);
      final Value value = new Value(attribute, boundingBox);
      return new EnvelopeIntersects(column, value);
    }
  }

  public static EnvelopeIntersects envelopeIntersects(final FieldDefinition attribute,
    final Geometry geometry) {
    return new EnvelopeIntersects(new Column(attribute),
      new Value(attribute, geometry.getBoundingBox()));
  }

  public static EnvelopeIntersects envelopeIntersects(final RecordDefinition recordDefinition,
    final BoundingBox boundingBox) {
    final FieldDefinition attribute = recordDefinition.getGeometryField();
    return envelopeIntersects(attribute, boundingBox);
  }

  public static EnvelopeIntersects envelopeIntersects(final String name,
    final BoundingBoxProxy boundingBox) {
    return new EnvelopeIntersects(new Column(name), new Value(boundingBox.getBoundingBox()));
  }

  public static Lower lower(final QueryValue value) {

    return new Lower(value);
  }

  public static RegexpReplace regexpReplace(final QueryValue value, final String pattern,
    final String replace) {
    return new RegexpReplace(value, pattern, replace);
  }

  public static RegexpReplace regexpReplace(final QueryValue value, final String pattern,
    final String replace, final String flags) {
    return new RegexpReplace(value, pattern, replace, flags);
  }

  public static Upper upper(final FieldDefinition fieldDefinition) {
    return upper(new Column(fieldDefinition));
  }

  public static Upper upper(final QueryValue value) {
    return new Upper(value);
  }

  public static Upper upper(final String name) {
    return upper(new Column(name));
  }
}

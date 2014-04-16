package com.revolsys.gis.converter.string;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;

public class BoundingBoxStringConverter implements StringConverter<BoundingBox> {
  @Override
  public Class<BoundingBox> getConvertedClass() {
    return BoundingBox.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public BoundingBox toObject(final Object value) {
    if (value instanceof Envelope) {
      final Envelope geometry = (Envelope)value;
      return geometry;
    }
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return geometry.getBoundingBox();
    } else if (value == null) {
      return new Envelope();
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public BoundingBox toObject(final String string) {
    return Envelope.create(string);
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

}

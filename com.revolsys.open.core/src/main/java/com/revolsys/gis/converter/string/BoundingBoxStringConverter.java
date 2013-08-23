package com.revolsys.gis.converter.string;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.gis.cs.BoundingBox;
import com.vividsolutions.jts.geom.Geometry;

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
    if (value instanceof BoundingBox) {
      final BoundingBox geometry = (BoundingBox)value;
      return geometry;
    }
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return BoundingBox.getBoundingBox(geometry);
    } else if (value == null) {
      return new BoundingBox();
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public BoundingBox toObject(final String string) {
    return BoundingBox.create(string);
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

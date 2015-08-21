package com.revolsys.gis.converter.string;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;

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
    if (value instanceof BoundingBoxDoubleGf) {
      final BoundingBoxDoubleGf geometry = (BoundingBoxDoubleGf)value;
      return geometry;
    }
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return geometry.getBoundingBox();
    } else if (value == null) {
      return BoundingBox.EMPTY;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public BoundingBox toObject(final String string) {
    return BoundingBoxDoubleGf.create(string);
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

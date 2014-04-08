package com.revolsys.gis.converter.string;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.io.wkt.WktParser;
import com.revolsys.io.wkt.WktWriter;
import com.revolsys.jts.geom.Geometry;

public class GeometryStringConverter implements StringConverter<Geometry> {
  @Override
  public Class<Geometry> getConvertedClass() {
    return Geometry.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public Geometry toObject(final Object value) {
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return geometry;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Geometry toObject(final String string) {
    return new WktParser().parseGeometry(string, false);
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      return WktWriter.toString(geometry, true);
    } else {
      return value.toString();
    }
  }

}

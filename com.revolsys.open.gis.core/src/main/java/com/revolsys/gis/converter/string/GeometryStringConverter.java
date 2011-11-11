package com.revolsys.gis.converter.string;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.gis.wkt.WktParser;
import com.revolsys.gis.wkt.WktWriter;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryStringConverter implements StringConverter<Geometry> {
  private static final WktParser WKT_READER = new WktParser();

  public boolean requiresQuotes() {
    return true;
  }

  public String toString(Geometry value) {
    return WktWriter.toString(value, true);
  }

  public Geometry toObject(String string) {
    return WKT_READER.parseGeometry(string);
  }

  public Geometry toObject(Object value) {
    if (value instanceof Geometry) {
      Geometry geometry = (Geometry)value;
      return geometry;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Class<Geometry> getConvertedClass() {
    return Geometry.class;
  }

}

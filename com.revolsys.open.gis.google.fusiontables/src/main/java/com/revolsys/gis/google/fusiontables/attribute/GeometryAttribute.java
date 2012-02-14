package com.revolsys.gis.google.fusiontables.attribute;

import java.io.StringReader;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.kml.KmlGeometryIterator;
import com.revolsys.io.kml.KmlXmlWriter;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryAttribute extends FusionTablesAttribute {
  public static void appendString(final StringBuffer buffer, final Object value) {
    if (value == null) {
      buffer.append("''");
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      buffer.append('\'');
      KmlXmlWriter.append(buffer, geometry);
      buffer.append('\'');
    } else {
      throw new IllegalArgumentException("Expecting a geometry");
    }
  }

  public GeometryAttribute(final String name) {
    super(name, DataTypes.GEOMETRY);
  }

  @Override
  public void appendValue(final StringBuffer sql, final Object object) {
    appendString(sql, object);
  }

  @Override
  public GeometryAttribute clone() {
    return new GeometryAttribute(getName());
  }

  @Override
  public Object parseString(final String string) {
    if (string.trim().length() == 0) {
      return null;
    } else {
      try {
        final KmlGeometryIterator geometryIterator = new KmlGeometryIterator(
          new StringReader(string));
        if (geometryIterator.hasNext()) {
          return geometryIterator.next();
        } else {
          return null;
        }
      } catch (Exception e) {
        return string;
      }
    }
  }
}

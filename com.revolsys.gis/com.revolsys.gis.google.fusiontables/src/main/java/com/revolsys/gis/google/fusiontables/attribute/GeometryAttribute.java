package com.revolsys.gis.google.fusiontables.attribute;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.kml.io.KmlGeometryIterator;
import com.revolsys.gis.kml.io.KmlWriterUtil;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryAttribute extends FusionTablesAttribute {
 public GeometryAttribute(String name) {
    super(name, DataTypes.GEOMETRY);
  }

  public void appendValue(StringBuffer sql, Object object) {
    if (object == null) {
      sql.append("''");
    } else if (object instanceof Geometry) {
      Geometry geometry = (Geometry)object;
      sql.append('\'');
      KmlWriterUtil.append(sql, geometry);
      sql.append('\'');
    } else {
      throw new IllegalArgumentException("Expecting a geometry");
    }
  }

  public Object parseString(String string) {
    if (string.trim().length() == 0) {
      return null;
    } else {
      final KmlGeometryIterator geometryIterator = new KmlGeometryIterator(
        new StringReader(string));
      if (geometryIterator.hasNext()) {
        return geometryIterator.next();
      } else {
        return null;
      }
    }
  }

  @Override
  public GeometryAttribute clone() {
    return new GeometryAttribute(getName());
  }
}

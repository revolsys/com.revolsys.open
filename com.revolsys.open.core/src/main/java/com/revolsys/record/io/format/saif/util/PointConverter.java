package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.record.io.format.saif.SaifConstants;

public class PointConverter implements OsnConverter {
  private String geometryClass = SaifConstants.POINT;

  private final GeometryFactory geometryFactory;

  public PointConverter(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public PointConverter(final GeometryFactory geometryFactory, final String geometryClass) {
    this.geometryFactory = geometryFactory;
    this.geometryClass = geometryClass;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put("type", this.geometryClass);
    Geometry geometry = null;

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("coords")) {
        Point coordinate = null;
        final String coordTypeName = iterator.nextObjectName();
        if (coordTypeName.equals("/Coord3D")) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          double z = iterator.nextDoubleAttribute("c3");
          if (z == 2147483648.0) {
            z = 0;
          }
          coordinate = new PointDouble(x, y, z);
        } else if (coordTypeName.equals("/Coord2D")) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          coordinate = new PointDouble(x, y);
        } else {
          iterator.throwParseError("Expecting Coord2D or Coord3D");
        }
        iterator.nextEndObject();
        geometry = this.geometryFactory.point(coordinate);
      } else {
        readAttribute(iterator, fieldName, values);
      }
      fieldName = iterator.nextFieldName();
    }
    if (!values.isEmpty()) {
      geometry.setUserData(values);
    }
    return geometry;
  }

  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    iterator.next();
    values.put(fieldName, iterator.getValue());
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    if (object instanceof Point) {
      final Point point = (Point)object;
      final int axisCount = point.getAxisCount();
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      serializer.startObject(this.geometryClass);
      serializer.fieldName("coords");
      if (axisCount == 2) {
        serializer.startObject("/Coord2D");
        serializer.attribute("c1", x, true);
        serializer.attribute("c2", y, false);
      } else {
        serializer.startObject("/Coord3D");
        serializer.attribute("c1", x, true);
        serializer.attribute("c2", y, true);
        if (Double.isNaN(z)) {
          serializer.attribute("c3", 0, false);
        } else {
          serializer.attribute("c3", z, false);
        }
      }
      serializer.endObject();
      serializer.endAttribute();

      final Map<String, Object> values = GeometryProperties.getGeometryProperties(point);
      writeAttributes(serializer, values);
      serializer.endObject();
    }
  }

  protected void writeAttribute(final OsnSerializer serializer, final Map<String, Object> values,
    final String name) throws IOException {
    final Object value = values.get(name);
    if (value != null) {
      serializer.endLine();
      serializer.attribute(name, value, false);
    }

  }

  protected void writeAttributes(final OsnSerializer serializer, final Map<String, Object> values)
    throws IOException {
    writeEnumAttribute(serializer, values, "qualifier");
  }

  protected void writeEnumAttribute(final OsnSerializer serializer,
    final Map<String, Object> values, final String name) throws IOException {
    final String value = (String)values.get(name);
    if (value != null) {
      serializer.endLine();
      serializer.attributeEnum(name, value, false);
    }
  }

}

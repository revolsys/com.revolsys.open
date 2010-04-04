package com.revolsys.gis.format.saif.io.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class PointConverter implements OsnConverter {
  private String geometryClass = "Point";

  private final GeometryFactory geometryFactory;

  public PointConverter(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public PointConverter(
    final GeometryFactory geometryFactory,
    final String geometryClass) {
    this.geometryFactory = geometryFactory;
    this.geometryClass = geometryClass;
  }

  public Object read(
    final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put("type", geometryClass);
    Geometry geometry = null;

    String attributeName = iterator.nextAttributeName();
    while (attributeName != null) {
      if (attributeName.equals("coords")) {
        Coordinate coordinate = null;
        final QName coordTypeName = iterator.nextObjectName();
        if (coordTypeName.equals(new QName("Coord3D"))) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          double z = iterator.nextDoubleAttribute("c3");
          if (z == 2147483648.0) {
            z = 0;
          }
          coordinate = new Coordinate(x, y, z);
        } else if (coordTypeName.equals(new QName("Coord2D"))) {
          final double x = iterator.nextDoubleAttribute("c1");
          final double y = iterator.nextDoubleAttribute("c2");
          coordinate = new Coordinate(x, y);
        } else {
          iterator.throwParseError("Expecting Coord2D or Coord3D");
        }
        iterator.nextEndObject();

        geometry = geometryFactory.createPoint(coordinate);
      } else {
        readAttribute(iterator, attributeName, values);
      }
      attributeName = iterator.nextAttributeName();
    }
    if (!values.isEmpty()) {
      geometry.setUserData(values);
    }
    return geometry;
  }

  protected void readAttribute(
    final OsnIterator iterator,
    final String attributeName,
    final Map<String, Object> values) {
    iterator.next();
    values.put(attributeName, iterator.getValue());
  }

  public void write(
    final OsnSerializer serializer,
    final Object object)
    throws IOException {
    if (object instanceof Point) {
      final Point point = (Point)object;
      final Coordinate coordinate = point.getCoordinate();
      serializer.startObject(geometryClass);
      serializer.attributeName("coords");
      if (Double.isNaN(coordinate.z)) {
        serializer.startObject("Coord2D");
        serializer.attribute("c1", coordinate.x, true);
        serializer.attribute("c2", coordinate.y, false);
        serializer.endObject();
      } else {
        serializer.startObject("Coord3D");
        serializer.attribute("c1", coordinate.x, true);
        serializer.attribute("c2", coordinate.y, true);
        serializer.attribute("c3", coordinate.z, false);
        serializer.endObject();
      }
      serializer.endAttribute();

      final Map<String, Object> values = JtsGeometryUtil.getGeometryProperties(point);
      writeAttributes(serializer, values);
      serializer.endObject();
    }
  }

  protected void writeAttribute(
    final OsnSerializer serializer,
    final Map<String, Object> values,
    final String name)
    throws IOException {
    final Object value = values.get(name);
    if (value != null) {
      serializer.endLine();
      serializer.attribute(name, value, false);
    }

  }

  protected void writeAttributes(
    final OsnSerializer serializer,
    final Map<String, Object> values)
    throws IOException {
    writeEnumAttribute(serializer, values, "qualifier");
  }

  protected void writeEnumAttribute(
    final OsnSerializer serializer,
    final Map<String, Object> values,
    final String name)
    throws IOException {
    final String value = (String)values.get(name);
    if (value != null) {
      serializer.endLine();
      serializer.attributeEnum(name, value, false);
    }
  }

}

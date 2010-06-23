package com.revolsys.gis.format.saif.io.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.LineString;

public class ArcConverter implements OsnConverter {
  private final GeometryFactory geometryFactory;

  private String geometryType = "Arc";

  public ArcConverter(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public ArcConverter(
    final GeometryFactory geometryFactory,
    final String geometryType) {
    this.geometryFactory = geometryFactory;
    this.geometryType = geometryType;
  }

  public Object read(
    final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put("type", geometryType);

    String attributeName = iterator.nextAttributeName();
    LineString geometry = null;
    while (attributeName != null) {
      if (attributeName.equals("pointList")) {
        final List<Coordinates> coordinates = new ArrayList<Coordinates>();
        while (iterator.next() != OsnIterator.END_LIST) {
          final QName pointName = iterator.nextObjectName();
          if (!pointName.equals(new QName("Point"))) {
            iterator.throwParseError("Expecting Point object");
          }
          final String coordsName = iterator.nextAttributeName();
          if (!coordsName.equals("coords")) {
            iterator.throwParseError("Expecting coords attribute");
          }
          final QName coordTypeName = iterator.nextObjectName();
          if (coordTypeName.equals(new QName("Coord3D"))) {
            final double x = iterator.nextDoubleAttribute("c1");
            final double y = iterator.nextDoubleAttribute("c2");
            final double z = iterator.nextDoubleAttribute("c3");
            coordinates.add(new DoubleCoordinates(x, y, z));
          } else if (coordTypeName.equals(new QName("Coord2D"))) {
            final double x = iterator.nextDoubleAttribute("c1");
            final double y = iterator.nextDoubleAttribute("c2");
            coordinates.add(new DoubleCoordinates(x, y));
          } else {
            iterator.throwParseError("Expecting Coord2D or Coord3D");
          }
          iterator.nextEndObject();
          iterator.nextEndObject();
        }
        geometry = geometryFactory.createLineString(coordinates);
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
    final Object value = iterator.nextValue();
    values.put(attributeName, value);
  }

  public void write(
    final OsnSerializer serializer,
    final Object object)
    throws IOException {
    final boolean writeAttributes = true;
    write(serializer, object, writeAttributes);
  }

  protected void write(
    final OsnSerializer serializer,
    final Object object,
    final boolean writeAttributes)
    throws IOException {
    if (object instanceof LineString) {
      final LineString line = (LineString)object;
      serializer.startObject(geometryType);

      serializer.attributeName("pointList");
      serializer.startCollection("List");
      CoordinatesList points = CoordinatesListUtil.get(line);
      for (int i = 0; i < points.size(); i++) {
        serializer.startObject("Point");
        serializer.attributeName("coords");
        final double x = points.getX(i);
        final double y = points.getY(i);
        final double z = points.getZ(i);
        if (Double.isNaN(z)) {
          serializer.startObject("Coord2D");
          serializer.attribute("c1", x, true);
          serializer.attribute("c2", y, false);
          serializer.endObject();
        } else {
          serializer.startObject("Coord3D");
          serializer.attribute("c1", x, true);
          serializer.attribute("c2", y, true);
          serializer.attribute("c3", z, false);
          serializer.endObject();
        }
        serializer.endAttribute();
        serializer.endObject();
      }
      serializer.endCollection();
      serializer.endAttribute();
      if (writeAttributes) {
        writeAttributes(serializer,
          JtsGeometryUtil.getGeometryProperties(line));
      }
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

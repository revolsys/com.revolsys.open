package com.revolsys.format.saif.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.format.saif.SaifConstants;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.gis.jts.GeometryProperties;

public class ArcConverter implements OsnConverter {
  private final GeometryFactory geometryFactory;

  private String geometryType = SaifConstants.ARC;

  public ArcConverter(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public ArcConverter(final GeometryFactory geometryFactory, final String geometryType) {
    this.geometryFactory = geometryFactory;
    this.geometryType = geometryType;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put(SaifConstants.TYPE, this.geometryType);

    String field = iterator.nextFieldName();
    LineString geometry = null;
    while (field != null) {
      if (field.equals("LineString")) {
        final List<Point> coordinates = new ArrayList<Point>();
        while (iterator.next() != OsnIterator.END_LIST) {
          final String pointName = iterator.nextObjectName();
          if (!pointName.equals("/Point")) {
            iterator.throwParseError("Expecting Point object");
          }
          final String coordsName = iterator.nextFieldName();
          if (!coordsName.equals("coords")) {
            iterator.throwParseError("Expecting coords attribute");
          }
          final String coordTypeName = iterator.nextObjectName();
          if (coordTypeName.equals("/Coord3D")) {
            final double x = iterator.nextDoubleAttribute("c1");
            final double y = iterator.nextDoubleAttribute("c2");
            final double z = iterator.nextDoubleAttribute("c3");
            coordinates.add(new PointDouble(x, y, z));
          } else if (coordTypeName.equals("/Coord2D")) {
            final double x = iterator.nextDoubleAttribute("c1");
            final double y = iterator.nextDoubleAttribute("c2");
            coordinates.add(new PointDouble(x, y));
          } else {
            iterator.throwParseError("Expecting Coord2D or Coord3D");
          }
          iterator.nextEndObject();
          iterator.nextEndObject();
        }
        geometry = this.geometryFactory.lineString(coordinates);
      } else {
        readAttribute(iterator, field, values);
      }
      field = iterator.nextFieldName();
    }
    if (!values.isEmpty()) {
      geometry.setUserData(values);
    }

    return geometry;
  }

  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    final Object value = iterator.nextValue();
    values.put(fieldName, value);
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    final boolean writeAttributes = true;
    write(serializer, object, writeAttributes);
  }

  protected void write(final OsnSerializer serializer, final Object object,
    final boolean writeAttributes) throws IOException {
    if (object instanceof LineString) {
      final LineString line = (LineString)object;
      serializer.startObject(this.geometryType);

      serializer.fieldName("LineString");
      serializer.startCollection("List");
      final LineString points = line;
      final int axisCount = points.getAxisCount();
      for (int i = 0; i < points.getVertexCount(); i++) {
        serializer.startObject(SaifConstants.POINT);
        serializer.fieldName("coords");
        final double x = points.getX(i);
        final double y = points.getY(i);
        final double z = points.getZ(i);
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
        serializer.endObject();
      }
      serializer.endCollection();
      serializer.endAttribute();
      if (writeAttributes) {
        writeAttributes(serializer, GeometryProperties.getGeometryProperties(line));
      }
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

package com.revolsys.gis.format.saif.io.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class OrientedArcConverter extends ArcConverter {
  private static final String GEOMETRY_CLASS = "OrientedArc";

  private final OsnConverterRegistry converters;

  public OrientedArcConverter(
    final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    super(geometryFactory, "Arc");
    this.converters = converters;
  }

  @Override
  public Object read(
    final OsnIterator iterator) {
    Geometry geometry = null;
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put("type", GEOMETRY_CLASS);
    String name = iterator.nextAttributeName();
    while (name != null) {
      if (name.equals("arc")) {
        final QName objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = (Geometry)osnConverter.read(iterator);
      } else {
        readAttribute(iterator, name, values);
      }
      name = iterator.nextAttributeName();
    }
    geometry.setUserData(values);
    return geometry;
  }

  @Override
  public void write(
    final OsnSerializer serializer,
    final Object object)
    throws IOException {
    if (object instanceof LineString) {
      final LineString lineString = (LineString)object;
      serializer.startObject(GEOMETRY_CLASS);
      serializer.attributeName("arc");
      super.write(serializer, object, false);
      serializer.endAttribute();
      final Map<String, Object> values = JtsGeometryUtil.getGeometryProperties(lineString);
      writeEnumAttribute(serializer, values, "qualifier");
      writeEnumAttribute(serializer, values, "traversalDirection");
      serializer.endObject();
    }
  }

}

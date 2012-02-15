package com.revolsys.io.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class ContourConverter extends ArcConverter {
  private static final String GEOMETRY_CLASS = "Contour";

  private final OsnConverterRegistry converters;

  public ContourConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    super(geometryFactory, "Arc");
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put("type", GEOMETRY_CLASS);
    Geometry geometry = null;

    String attributeName = iterator.nextAttributeName();
    while (attributeName != null) {
      if (attributeName.equals("arc")) {
        final QName objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = (Geometry)osnConverter.read(iterator);
      } else if (attributeName.equals("value")) {
        final double value = iterator.nextDoubleValue();
        values.put("value", new Double(value));
      } else {
        readAttribute(iterator, attributeName, values);
      }
      attributeName = iterator.nextAttributeName();
    }
    if (geometry != null) {
      if (!values.isEmpty()) {
        geometry.setUserData(values);
      }
    }
    return geometry;
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object)
    throws IOException {
    if (object instanceof LineString) {
      final LineString lineString = (LineString)object;
      serializer.startObject(GEOMETRY_CLASS);
      serializer.attributeName("arc");
      super.write(serializer, object);
      serializer.endAttribute();
      final Map<String, Object> values = JtsGeometryUtil.getGeometryProperties(lineString);
      writeEnumAttribute(serializer, values, "form");
      writeEnumAttribute(serializer, values, "qualifier");
      writeAttribute(serializer, values, "value");
      serializer.endObject();
    }
  }

}

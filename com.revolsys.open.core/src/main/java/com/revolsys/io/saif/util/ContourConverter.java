package com.revolsys.io.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.io.saif.SaifConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;

public class ContourConverter extends ArcConverter {
  private static final String GEOMETRY_CLASS = SaifConstants.CONTOUR;

  private final OsnConverterRegistry converters;

  public ContourConverter(final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    super(geometryFactory, SaifConstants.ARC);
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put(SaifConstants.TYPE, GEOMETRY_CLASS);
    Geometry geometry = null;

    String attributeName = iterator.nextAttributeName();
    while (attributeName != null) {
      if (attributeName.equals("arc")) {
        final String objectName = iterator.nextObjectName();
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

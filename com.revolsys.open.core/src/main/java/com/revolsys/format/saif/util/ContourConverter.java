package com.revolsys.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.format.saif.SaifConstants;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;

public class ContourConverter extends ArcConverter {
  private static final String GEOMETRY_CLASS = SaifConstants.CONTOUR;

  private final OsnConverterRegistry converters;

  public ContourConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    super(geometryFactory, SaifConstants.ARC);
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put(SaifConstants.TYPE, GEOMETRY_CLASS);
    Geometry geometry = null;

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("arc")) {
        final String objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = this.converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = (Geometry)osnConverter.read(iterator);
      } else if (fieldName.equals("value")) {
        final double value = iterator.nextDoubleValue();
        values.put("value", new Double(value));
      } else {
        readAttribute(iterator, fieldName, values);
      }
      fieldName = iterator.nextFieldName();
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
      serializer.fieldName("arc");
      super.write(serializer, object);
      serializer.endAttribute();
      final Map<String, Object> values = GeometryProperties.getGeometryProperties(lineString);
      writeEnumAttribute(serializer, values, "form");
      writeEnumAttribute(serializer, values, "qualifier");
      writeAttribute(serializer, values, "value");
      serializer.endObject();
    }
  }

}

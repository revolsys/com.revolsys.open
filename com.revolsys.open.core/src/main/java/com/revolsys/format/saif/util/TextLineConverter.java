package com.revolsys.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.format.saif.SaifConstants;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.gis.jts.GeometryProperties;

public class TextLineConverter implements OsnConverter {
  private static final String TYPE = "type";

  private final OsnConverterRegistry converters;

  public TextLineConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<String, Object>();
    values.put(TYPE, SaifConstants.TEXT_LINE);
    Geometry geometry = null;

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("position")) {
        final String objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = this.converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = (Geometry)osnConverter.read(iterator);
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
      serializer.startObject(SaifConstants.TEXT_LINE);
      serializer.fieldName("position");
      final OsnConverter osnConverter = this.converters.getConverter(SaifConstants.POINT);
      osnConverter.write(serializer, point);
      serializer.endAttribute();

      final Map<String, Object> values = GeometryProperties.getGeometryProperties(point);
      writeAttributes(serializer, values);
      serializer.endObject();
    }
  }

  protected void writeAttribute(final OsnSerializer serializer, final String name,
    final Object value) throws IOException {
    if (value != null) {
      serializer.endLine();
      serializer.attribute(name, value, false);
    }

  }

  protected void writeAttributes(final OsnSerializer serializer, final Map<String, Object> values)
    throws IOException {
    for (final Entry<String, Object> entry : values.entrySet()) {
      final String key = entry.getKey();
      if (key != TYPE) {
        writeAttribute(serializer, key, entry.getValue());
      }
    }
  }

}

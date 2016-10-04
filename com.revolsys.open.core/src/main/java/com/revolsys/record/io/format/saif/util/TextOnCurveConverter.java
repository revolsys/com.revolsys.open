package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.record.io.format.saif.SaifConstants;

public class TextOnCurveConverter implements OsnConverter {
  private static final String TYPE = "type";

  private final OsnConverterRegistry converters;

  private final GeometryFactory geometryFactory;

  public TextOnCurveConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    this.geometryFactory = geometryFactory;
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    final Map<String, Object> values = new TreeMap<>();
    values.put(TYPE, SaifConstants.TEXT_ON_CURVE);
    Geometry geometry = null;
    final List<Point> points = new ArrayList<>();

    String fieldName = iterator.nextFieldName();
    while (fieldName != null) {
      if (fieldName.equals("characters")) {
        while (iterator.next() != OsnIterator.END_LIST) {
          final String objectName = iterator.nextObjectName();
          final OsnConverter osnConverter = this.converters.getConverter(objectName);
          if (osnConverter == null) {
            iterator.throwParseError("No Geometry Converter for " + objectName);
          }
          points.add((Point)osnConverter.read(iterator));
        }
      }
      fieldName = iterator.nextFieldName();
    }
    geometry = this.geometryFactory.punctual(points);
    for (int i = 0; i < points.size(); i++) {
      final Point originalPoint = points.get(0);
      final Point geometryPoint = (Point)geometry.getGeometry(i);
      geometryPoint.setExtendedData(originalPoint.getExtendedData());
    }
    geometry.setExtendedData(values);
    return geometry;
  }

  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    iterator.next();
    values.put(fieldName, iterator.getValue());
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    if (object instanceof Punctual) {
      final Punctual punctual = (Punctual)object;
      serializer.startObject(SaifConstants.TEXT_ON_CURVE);
      serializer.fieldName("characters");
      serializer.startCollection("List");
      final OsnConverter osnConverter = this.converters.getConverter(SaifConstants.TEXT_LINE);
      for (final Point point : punctual.points()) {
        osnConverter.write(serializer, point);
      }
      serializer.endCollection();
      serializer.endAttribute();

      final Map<String, Object> values = GeometryProperties.getGeometryProperties(punctual);
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

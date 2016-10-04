package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.record.io.format.saif.SaifConstants;

public class OrientedArcConverter extends ArcConverter {
  private static final String GEOMETRY_CLASS = SaifConstants.ORIENTED_ARC;

  private final OsnConverterRegistry converters;

  public OrientedArcConverter(final GeometryFactory geometryFactory,
    final OsnConverterRegistry converters) {
    super(geometryFactory, SaifConstants.ARC);
    this.converters = converters;
  }

  @Override
  public Object read(final OsnIterator iterator) {
    Geometry geometry = null;
    final Map<String, Object> values = new TreeMap<>();
    values.put("type", GEOMETRY_CLASS);
    String name = iterator.nextFieldName();
    while (name != null) {
      if (name.equals("arc")) {
        final String objectName = iterator.nextObjectName();
        final OsnConverter osnConverter = this.converters.getConverter(objectName);
        if (osnConverter == null) {
          iterator.throwParseError("No Geometry Converter for " + objectName);
        }
        geometry = (Geometry)osnConverter.read(iterator);
      } else {
        readAttribute(iterator, name, values);
      }
      name = iterator.nextFieldName();
    }
    geometry.setExtendedData(values);
    return geometry;
  }

  @Override
  public void write(final OsnSerializer serializer, final Object object) throws IOException {
    if (object instanceof LineString) {
      final LineString lineString = (LineString)object;
      serializer.startObject(GEOMETRY_CLASS);
      serializer.fieldName("arc");
      super.write(serializer, object, false);
      serializer.endAttribute();
      final Map<String, Object> values = GeometryProperties.getGeometryProperties(lineString);
      writeEnumAttribute(serializer, values, "qualifier");
      writeEnumAttribute(serializer, values, "traversalDirection");
      serializer.endObject();
    }
  }

}

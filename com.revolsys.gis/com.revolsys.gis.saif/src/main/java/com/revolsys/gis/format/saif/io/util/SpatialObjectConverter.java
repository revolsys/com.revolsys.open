package com.revolsys.gis.format.saif.io.util;

import javax.xml.namespace.QName;

public class SpatialObjectConverter implements OsnConverter {
  private final OsnConverterRegistry converters;

  public SpatialObjectConverter(
    final OsnConverterRegistry converters) {
    this.converters = converters;
  }

  public Object read(
    final OsnIterator iterator) {
    final String name = iterator.nextAttributeName();
    if (!name.equals("geometry")) {
      iterator.throwParseError("No geometry attribute");
    }
    final QName objectName = iterator.nextObjectName();
    final OsnConverter osnConverter = converters.getConverter(objectName);
    if (osnConverter == null) {
      iterator.throwParseError("No Geometry Converter for " + objectName);
    }
    final Object geometry = osnConverter.read(iterator);
    iterator.nextEndObject();
    return geometry;
  }

  public void write(
    final OsnSerializer serializer,
    final Object object) {
  }

}

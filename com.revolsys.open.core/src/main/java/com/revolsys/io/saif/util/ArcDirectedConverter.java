package com.revolsys.io.saif.util;

import java.io.IOException;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;

public class ArcDirectedConverter extends ArcConverter {

  public ArcDirectedConverter(final GeometryFactory geometryFactory) {
    super(geometryFactory, "/ArcDirected");
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer,
    final Map<String, Object> values) throws IOException {
    writeEnumAttribute(serializer, values, "flowDirection");
    writeEnumAttribute(serializer, values, "qualifier");
  }

}

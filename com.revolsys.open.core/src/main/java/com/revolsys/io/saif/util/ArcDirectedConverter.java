package com.revolsys.io.saif.util;

import java.io.IOException;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.saif.SaifConstants;

public class ArcDirectedConverter extends ArcConverter {

  public ArcDirectedConverter(final GeometryFactory geometryFactory) {
    super(geometryFactory, SaifConstants.ARC_DIRECTED);
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer,
    final Map<String, Object> values) throws IOException {
    writeEnumAttribute(serializer, values, "flowDirection");
    writeEnumAttribute(serializer, values, "qualifier");
  }

}

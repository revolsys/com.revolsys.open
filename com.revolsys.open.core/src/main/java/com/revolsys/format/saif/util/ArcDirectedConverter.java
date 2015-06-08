package com.revolsys.format.saif.util;

import java.io.IOException;
import java.util.Map;

import com.revolsys.format.saif.SaifConstants;
import com.revolsys.jts.geom.GeometryFactory;

public class ArcDirectedConverter extends ArcConverter {

  public ArcDirectedConverter(final GeometryFactory geometryFactory) {
    super(geometryFactory, SaifConstants.ARC_DIRECTED);
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer, final Map<String, Object> values)
    throws IOException {
    writeEnumAttribute(serializer, values, "flowDirection");
    writeEnumAttribute(serializer, values, "qualifier");
  }

}

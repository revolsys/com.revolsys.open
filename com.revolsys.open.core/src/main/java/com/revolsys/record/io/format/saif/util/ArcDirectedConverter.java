package com.revolsys.record.io.format.saif.util;

import java.io.IOException;
import java.util.Map;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.saif.SaifConstants;

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

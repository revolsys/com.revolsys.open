package com.revolsys.format.saif.util;

import java.io.IOException;
import java.util.Map;

import com.revolsys.format.saif.SaifConstants;
import com.revolsys.geometry.model.GeometryFactory;

public class AlignedPointConverter extends PointConverter {
  public AlignedPointConverter(final GeometryFactory geometryFactory) {
    super(geometryFactory, SaifConstants.ALIGNED_POINT);
  }

  /**
   * north, directionIndicator are handled by the default handling
   */
  @Override
  protected void readAttribute(final OsnIterator iterator, final String fieldName,
    final Map<String, Object> values) {
    if (fieldName.equals("alignment")) {
      values.put("alignment", new Double(iterator.nextDoubleValue()));
    } else {
      super.readAttribute(iterator, fieldName, values);
    }
  }

  @Override
  protected void writeAttributes(final OsnSerializer serializer, final Map<String, Object> values)
    throws IOException {
    writeAttribute(serializer, values, "alignment");
    writeAttribute(serializer, values, "directionIndicator");
    writeEnumAttribute(serializer, values, "north");
    writeEnumAttribute(serializer, values, "qualifier");
  }

}

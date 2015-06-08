package com.revolsys.data.validator;

import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.jts.geom.Geometry;

public class GeometryFieldValidator implements FieldValueValidator {

  @Override
  public boolean isValid(final FieldDefinition fieldDefinition, final Object value) {
    return value instanceof Geometry;

  }
}

package com.revolsys.record.io.format.csv;

import org.jeometry.common.datatype.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.schema.FieldDefinition;

public class GeometryFieldDefinition extends FieldDefinition {

  private final GeometryFactory geometryFactory;

  public GeometryFieldDefinition(final GeometryFactory geometryFactory, final String name,
    final DataType type, final boolean required) {
    super(name, type, required);
    this.geometryFactory = geometryFactory;
  }

  @Override
  public FieldDefinition clone() {
    final String name = getName();
    final DataType dataType = getDataType();
    final boolean required = isRequired();
    return new GeometryFieldDefinition(this.geometryFactory, name, dataType, required);
  }

  @Override
  public <V> V toFieldValueException(final Object value) {
    Geometry geometry;
    if (value == null) {
      return null;
    } else if (value instanceof Geometry) {
      geometry = (Geometry)value;
      geometry = geometry.convertGeometry(this.geometryFactory);
    } else {
      geometry = this.geometryFactory.geometry(value.toString());
    }
    return getDataType().toObject(geometry);
  }
}

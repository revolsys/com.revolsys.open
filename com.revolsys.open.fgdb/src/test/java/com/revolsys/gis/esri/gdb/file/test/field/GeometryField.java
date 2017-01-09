package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.property.FieldProperties;

public class GeometryField extends FgdbField {
  public GeometryField(final String name, final DataType type, final boolean required,
    final GeometryFactory geometryFactory) {
    super(name, type, required);
    setProperty(FieldProperties.GEOMETRY_FACTORY, geometryFactory);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final ByteBuffer buffer) throws IOException {
    // final Geometry geometry = null;
    // final long length = FgdbReader.readVarUInt(buffer);
    // buffer.read(new byte[(int)length]);
    // return (T)geometry;
    return null;
  }
}

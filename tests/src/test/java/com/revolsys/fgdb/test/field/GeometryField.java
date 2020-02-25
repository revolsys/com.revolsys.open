package com.revolsys.fgdb.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.GeometryFactory;

public class GeometryField extends FgdbField {
  public GeometryField(final String name, final DataType type, final boolean required,
    final GeometryFactory geometryFactory) {
    super(name, type, required);
    setGeometryFactory(geometryFactory);
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

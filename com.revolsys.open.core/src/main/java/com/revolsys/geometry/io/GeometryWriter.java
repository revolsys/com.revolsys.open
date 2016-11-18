package com.revolsys.geometry.io;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;

public interface GeometryWriter extends Writer<Geometry> {
  static boolean isWritable(final Object source) {
    return IoFactory.isAvailable(GeometryWriterFactory.class, source);
  }

  static GeometryWriter newGeometryWriter(final Object source) {
    final GeometryWriterFactory factory = IoFactory.factory(GeometryWriterFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.newGeometryWriter(source);
    }
  }

  void setGeometryFactory(GeometryFactory geometryFactory);
}

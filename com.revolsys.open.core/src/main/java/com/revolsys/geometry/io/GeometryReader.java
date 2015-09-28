package com.revolsys.geometry.io;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;

public interface GeometryReader extends Reader<Geometry> {
  static boolean isReadable(final Object source) {
    final GeometryReaderFactory factory = IoFactory.factory(GeometryReaderFactory.class, source);
    if (factory == null || !factory.isGeometrySupported()) {
      return false;
    } else {
      return true;
    }
  }

  static GeometryReader newGeometryReader(final Object source) {
    final GeometryReaderFactory factory = IoFactory.factory(GeometryReaderFactory.class, source);
    if (factory == null || !factory.isGeometrySupported()) {
      return null;
    } else {
      return factory.newGeometryReader(source);
    }
  }
}

package com.revolsys.geometry.io;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.spring.resource.Resource;

public interface GeometryReader extends Reader<Geometry> {
  static boolean isReadable(final Object source) {
    final GeometryReaderFactory readerFactory = IoFactory.factory(GeometryReaderFactory.class,
      source);
    if (readerFactory == null || !readerFactory.isGeometrySupported()) {
      return false;
    } else {
      return true;
    }
  }

  static GeometryReader newGeometryReader(final Object source) {
    final GeometryReaderFactory readerFactory = IoFactory.factory(GeometryReaderFactory.class,
      source);
    if (readerFactory == null || !readerFactory.isGeometrySupported()) {
      return null;
    } else {
      final Resource resource = readerFactory.getZipResource(source);
      return readerFactory.newGeometryReader(resource);
    }
  }

  GeometryFactory getGeometryFactory();
}

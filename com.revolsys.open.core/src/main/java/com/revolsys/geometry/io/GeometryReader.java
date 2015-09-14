package com.revolsys.geometry.io;

import java.util.Iterator;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.IteratorReader;

public class GeometryReader extends IteratorReader<Geometry> {
  public static GeometryReader create(final Object source) {
    final GeometryReaderFactory factory = IoFactory.factory(GeometryReaderFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.createGeometryReader(source);
    }
  }

  public static boolean isReadable(final Object source) {
    return IoFactoryRegistry.isAvailable(GeometryReaderFactory.class, source);
  }

  public GeometryReader(final Iterator<Geometry> iterator) {
    super(iterator);
  }
}

package com.revolsys.gis.geometry.io;

import com.revolsys.io.AbstractIoFactory;

public abstract class AbstractGeometryReaderFactory extends AbstractIoFactory
  implements GeometryReaderFactory {

  public AbstractGeometryReaderFactory(
    final String name) {
    super(name);
  }
}

package com.revolsys.gis.geometry.io;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.Reader;
import com.revolsys.io.IoFactory;
import com.vividsolutions.jts.geom.Geometry;

public interface GeometryReaderFactory extends IoFactory {
  Reader<Geometry> createGeometryReader(
    final Resource resource);
}

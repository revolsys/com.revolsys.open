package com.revolsys.gis.geometry.io;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.io.IoFactory;

public interface GeometryReaderFactory extends IoFactory {
  GeometryReader createGeometryReader(
    final Resource resource);
}

package com.revolsys.gis.geometry.io;

import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.IoFactory;

public interface GeometryReaderFactory extends IoFactory {
  GeometryReader createGeometryReader(final Resource resource);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isBinary();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);
}

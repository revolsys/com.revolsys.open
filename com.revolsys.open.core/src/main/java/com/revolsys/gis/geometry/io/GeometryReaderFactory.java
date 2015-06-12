package com.revolsys.gis.geometry.io;

import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.FileIoFactory;

public interface GeometryReaderFactory extends FileIoFactory {
  GeometryReader createGeometryReader(final Resource resource);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);
}

package com.revolsys.gis.geometry.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;

public interface GeometryWriterFactory extends IoFactory {
  Writer<Geometry> createGeometryWriter(Resource resource);

  Writer<Geometry> createGeometryWriter(String baseName, OutputStream out);

  Writer<Geometry> createGeometryWriter(String baseName, OutputStream out,
    Charset charset);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);
}

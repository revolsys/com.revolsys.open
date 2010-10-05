package com.revolsys.gis.geometry.io;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public interface GeometryWriterFactory extends IoFactory {
  Writer<Geometry> createGeometryWriter(
    Resource resource);

  Writer<Geometry> createGeometryWriter(
    String baseName,
    OutputStream out);

  Writer<Geometry> createGeometryWriter(
    String baseName,
    OutputStream out,
    Charset charset);
}

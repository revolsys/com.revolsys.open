package com.revolsys.geometry.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public interface GeometryWriterFactory extends FileIoFactory, IoFactoryWithCoordinateSystem {
  default GeometryWriter newGeometryWriter(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newGeometryWriter(resource);
  }

  /**
   * Construct a new writer to write to the specified resource.
   *
   * @param resource The resource to write to.
   * @return The writer.
   */
  default GeometryWriter newGeometryWriter(final Resource resource) {
    final OutputStream out = resource.newBufferedOutputStream();
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return newGeometryWriter(baseName, out);
  }

  /**
   * Construct a new reader for the file using the ({@link ArrayGeometryFactory}).
   *
   * @param baseName The base file name to write to.
   * @param out The output stream to write to.
   * @return The writer.
   */
  default GeometryWriter newGeometryWriter(final String baseName, final OutputStream out) {
    return newGeometryWriter(baseName, out, StandardCharsets.UTF_8);

  }

  GeometryWriter newGeometryWriter(String baseName, OutputStream out, Charset charset);
}

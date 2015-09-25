package com.revolsys.geometry.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public interface GeometryWriterFactory extends FileIoFactory, IoFactoryWithCoordinateSystem {
  default GeometryWriter createGeometryWriter(final Object source) {
    final Resource resource = com.revolsys.spring.resource.Resource.getResource(source);
    return createGeometryWriter(resource);
  }

  /**
   * Create a writer to write to the specified resource.
   *
   * @param resource The resource to write to.
   * @return The writer.
   */
  default GeometryWriter createGeometryWriter(final Resource resource) {
    final OutputStream out = resource.newBufferedOutputStream();
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createGeometryWriter(baseName, out);
  }

  /**
   * Create a reader for the file using the ({@link ArrayGeometryFactory}).
   *
   * @param baseName The base file name to write to.
   * @param out The output stream to write to.
   * @return The writer.
   */
  default GeometryWriter createGeometryWriter(final String baseName, final OutputStream out) {
    return createGeometryWriter(baseName, out, StandardCharsets.UTF_8);

  }

  GeometryWriter createGeometryWriter(String baseName, OutputStream out, Charset charset);
}

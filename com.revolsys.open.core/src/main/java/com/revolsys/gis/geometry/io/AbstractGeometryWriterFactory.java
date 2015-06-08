package com.revolsys.gis.geometry.io;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.spring.SpringUtil;

public abstract class AbstractGeometryWriterFactory extends AbstractIoFactory implements
  GeometryWriterFactory {

  public static Writer<Geometry> createWriter(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeometryWriterFactory writerFactory = ioFactoryRegistry.getFactoryByResource(
      GeometryWriterFactory.class, resource);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<Geometry> writer = writerFactory.createGeometryWriter(resource);
      return writer;
    }
  }

  public AbstractGeometryWriterFactory(final String name) {
    super(name);
  }

  /**
   * Create a writer to write to the specified resource.
   *
   * @param resource The resource to write to.
   * @return The writer.
   */
  @Override
  public Writer<Geometry> createGeometryWriter(final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
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
  @Override
  public Writer<Geometry> createGeometryWriter(final String baseName, final OutputStream out) {
    return createGeometryWriter(baseName, out, StandardCharsets.UTF_8);

  }

}

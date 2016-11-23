package com.revolsys.record.io.format.pointz;

import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.io.GeometryWriterFactory;
import com.revolsys.record.io.GeometryRecordReaderFactory;
import com.revolsys.spring.resource.OutputStreamResource;
import com.revolsys.spring.resource.Resource;

public class PointZIoFactory extends GeometryRecordReaderFactory implements GeometryWriterFactory {
  public static final short VERSION = 1;
  public static final String FILE_TYPE_POINTZ = "POINTZ";

  public PointZIoFactory() {
    super("Compact Binary PointZ");
    addMediaTypeAndFileExtension("application/x-revolsys-pointz", "pointz");

  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource) {
    return new PointZGeometryReader(resource);
  }

  @Override
  public GeometryWriter newGeometryWriter(final Resource resource) {
    return new PointZGeometryWriter(resource);
  }

  @Override
  public GeometryWriter newGeometryWriter(final String baseName, final OutputStream out,
    final Charset charset) {
    final OutputStreamResource resource = new OutputStreamResource(baseName, out);
    return newGeometryWriter(resource);
  }
}

package com.revolsys.record.io.format.pointz;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.io.GeometryWriterFactory;
import com.revolsys.record.io.GeometryRecordReaderFactory;
import com.revolsys.spring.resource.OutputStreamResource;
import com.revolsys.spring.resource.Resource;

public class PointZ extends GeometryRecordReaderFactory implements GeometryWriterFactory {

  public static final String FILE_EXTENSION = "pointz";

  public static final String FILE_TYPE_HEADER = "POINTZ";

  public static final byte[] FILE_TYPE_HEADER_BYTES = FILE_TYPE_HEADER
    .getBytes(StandardCharsets.UTF_8);

  public static final int HEADER_SIZE = 28;

  public static final String MIME_TYPE = "application/x-revolsys-pointz";

  public static final int RECORD_SIZE = 12;

  public static final short VERSION = 1;

  public PointZ() {
    super("Compact Binary PointZ");
    addMediaTypeAndFileExtension(MIME_TYPE, FILE_EXTENSION);

  }

  @Override
  public boolean isCustomFieldsSupported() {
    return false;
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

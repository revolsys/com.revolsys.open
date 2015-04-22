package com.revolsys.format.gml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.io.GeometryReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class GmlIoFactory extends AbstractRecordAndGeometryWriterFactory
implements GeometryReaderFactory {
  public GmlIoFactory() {
    super(GmlConstants.FORMAT_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(GmlConstants.MEDIA_TYPE,
      GmlConstants.FILE_EXTENSION);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final GmlGeometryIterator iterator = new GmlGeometryIterator(resource);
    return new GeometryReader(iterator);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new GmlRecordWriter(recordDefinition, writer);
  }

  @Override
  public boolean isBinary() {
    return false;
  }
}

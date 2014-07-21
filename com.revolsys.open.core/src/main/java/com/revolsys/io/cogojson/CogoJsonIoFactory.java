package com.revolsys.io.cogojson;

import java.io.IOException;
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
import com.revolsys.io.geojson.GeoJsonConstants;
import com.revolsys.io.geojson.GeoJsonGeometryIterator;
import com.revolsys.io.geojson.GeoJsonRecordWriter;

public class CogoJsonIoFactory extends
  AbstractRecordAndGeometryWriterFactory implements GeometryReaderFactory {

  public CogoJsonIoFactory() {
    super(GeoJsonConstants.COGO_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(GeoJsonConstants.COGO_MEDIA_TYPE,
      GeoJsonConstants.COGO_FILE_EXTENSION);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new GeoJsonRecordWriter(writer, true);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    try {
      final GeoJsonGeometryIterator iterator = new GeoJsonGeometryIterator(
        resource);
      return new GeometryReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public boolean isBinary() {
    return false;
  }
}

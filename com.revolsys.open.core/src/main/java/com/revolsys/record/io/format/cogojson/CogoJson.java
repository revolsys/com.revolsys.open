package com.revolsys.record.io.format.cogojson;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.geojson.GeoJsonGeometryIterator;
import com.revolsys.record.io.format.geojson.GeoJsonRecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class CogoJson extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory, GeometryReaderFactory {
  public static final String COGO_LINE_STRING = "CogoLineString";

  public static final String COGO_MULTI_LINE_STRING = "CogoMultiLineString";

  public static final String COGO_MULTI_POLYGON = "CogoMultiPolygon";

  public static final String COGO_POLYGON = "CogoPolygon";

  public CogoJson() {
    super("CogoJSON");
    addMediaTypeAndFileExtension("application/x-cogo+json", "cogojson");
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource) {
    final GeoJsonGeometryIterator iterator = new GeoJsonGeometryIterator(resource);
    return iterator;
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new GeoJsonRecordWriter(writer, true);
  }
}

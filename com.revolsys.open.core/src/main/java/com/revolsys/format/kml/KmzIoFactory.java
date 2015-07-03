package com.revolsys.format.kml;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Writer;

public class KmzIoFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory, MapWriterFactory, GeometryReaderFactory {

  public KmzIoFactory() {
    super(Kml22Constants.KMZ_FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.KMZ_MEDIA_TYPE, Kml22Constants.KMZ_FILE_EXTENSION);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final KmzGeometryIterator iterator = new KmzGeometryIterator(resource);
    return new GeometryReader(iterator);
  }

  @Override
  public MapWriter createMapWriter(final java.io.Writer out) {
    throw new IllegalArgumentException("Cannot use a writer");
  }

  @Override
  public MapWriter createMapWriter(final OutputStream out) {
    return new KmzMapWriter(out);
  }

  @Override
  public MapWriter createMapWriter(final OutputStream out, final Charset charset) {
    return createMapWriter(out);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return new KmzRecordWriter(outputStream, charset);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return KmlIoFactory.COORDINATE_SYSTEMS;
  }

  @Override
  public boolean isBinary() {
    return true;
  }
}

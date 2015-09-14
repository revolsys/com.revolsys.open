package com.revolsys.format.kml;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

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
  public RecordWriter createRecordWriter(final String baseName,
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

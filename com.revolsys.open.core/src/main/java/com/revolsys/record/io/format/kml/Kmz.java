package com.revolsys.record.io.format.kml;

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

public class Kmz extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory, MapWriterFactory, GeometryReaderFactory {

  public Kmz() {
    super(Kml22Constants.KMZ_FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.KMZ_MEDIA_TYPE, Kml22Constants.KMZ_FILE_EXTENSION);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return Kml.COORDINATE_SYSTEMS;
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource) {
    final KmzGeometryIterator iterator = new KmzGeometryIterator(resource);
    return iterator;
  }

  @Override
  public MapWriter newMapWriter(final java.io.Writer out) {
    throw new IllegalArgumentException("Cannot use a writer");
  }

  @Override
  public MapWriter newMapWriter(final OutputStream out) {
    return new KmzMapWriter(out);
  }

  @Override
  public MapWriter newMapWriter(final OutputStream out, final Charset charset) {
    return newMapWriter(out);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return new KmzRecordWriter(outputStream, charset);
  }
}

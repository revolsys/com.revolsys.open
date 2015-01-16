package com.revolsys.io.kml;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.io.GeometryReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public class KmzIoFactory extends AbstractRecordAndGeometryWriterFactory
implements MapWriterFactory, GeometryReaderFactory {

  public KmzIoFactory() {
    super(Kml22Constants.KMZ_FORMAT_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(Kml22Constants.KMZ_MEDIA_TYPE,
      Kml22Constants.KMZ_FILE_EXTENSION);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final KmzGeometryIterator iterator = new KmzGeometryIterator(resource);
    return new GeometryReader(iterator);
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
  public MapWriter getMapWriter(final java.io.Writer out) {
    throw new IllegalArgumentException("Cannot use a writer");
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out) {
    return new KmzMapWriter(out);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out, final Charset charset) {
    return getMapWriter(out);
  }

  @Override
  public MapWriter getMapWriter(final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    return getMapWriter(out);
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return KmlIoFactory.COORDINATE_SYSTEMS.contains(coordinateSystem);
  }

  @Override
  public boolean isCustomAttributionSupported() {
    return true;
  }

  @Override
  public boolean isGeometrySupported() {
    return true;
  }
}

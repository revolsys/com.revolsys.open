package com.revolsys.format.kml;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.AbstractRecordAndGeometryWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public class KmlIoFactory extends AbstractRecordAndGeometryWriterFactory
implements MapWriterFactory, GeometryReaderFactory {

  public static final Set<CoordinateSystem> COORDINATE_SYSTEMS = Collections.singleton(EpsgCoordinateSystems.wgs84());

  public KmlIoFactory() {
    super(Kml22Constants.KML_FORMAT_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(Kml22Constants.KML_MEDIA_TYPE,
      Kml22Constants.KML_FILE_EXTENSION);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final KmlGeometryIterator iterator = new KmlGeometryIterator(resource);
    return new GeometryReader(iterator);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new KmlRecordWriter(writer);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return COORDINATE_SYSTEMS;
  }

  @Override
  public MapWriter getMapWriter(final java.io.Writer out) {
    return new KmlMapWriter(out);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out) {
    final java.io.Writer writer = FileUtil.createUtf8Writer(out);
    final BufferedWriter bufferedWriter = new BufferedWriter(writer);
    return getMapWriter(bufferedWriter);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out, final Charset charset) {
    return getMapWriter(out);
  }

  @Override
  public MapWriter getMapWriter(final Resource resource) {
    final java.io.Writer writer = SpringUtil.getWriter(resource);
    return getMapWriter(writer);
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public boolean isCoordinateSystemSupported(
    final CoordinateSystem coordinateSystem) {
    return COORDINATE_SYSTEMS.contains(coordinateSystem);
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

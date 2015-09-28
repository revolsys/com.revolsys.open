package com.revolsys.record.io.format.kml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class Kml extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory, MapWriterFactory, GeometryReaderFactory {

  public static final Set<CoordinateSystem> COORDINATE_SYSTEMS = Collections
    .singleton(EpsgCoordinateSystems.wgs84());

  public Kml() {
    super(Kml22Constants.KML_FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.KML_MEDIA_TYPE, Kml22Constants.KML_FILE_EXTENSION);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return COORDINATE_SYSTEMS;
  }

  @Override
  public GeometryReader newGeometryReader(final Resource resource) {
    final KmlGeometryIterator iterator = new KmlGeometryIterator(resource);
    return iterator;
  }

  @Override
  public MapWriter newMapWriter(final java.io.Writer out) {
    return new KmlMapWriter(out);
  }

  @Override
  public MapWriter newMapWriter(final OutputStream out, final Charset charset) {
    return newMapWriter(out);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new KmlRecordWriter(writer);
  }
}

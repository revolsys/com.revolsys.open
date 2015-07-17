package com.revolsys.format.kml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.io.RecordWriter;
import com.revolsys.data.record.io.RecordWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.geometry.io.GeometryReader;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;

public class KmlIoFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory, MapWriterFactory, GeometryReaderFactory {

  public static final Set<CoordinateSystem> COORDINATE_SYSTEMS = Collections
    .singleton(EpsgCoordinateSystems.wgs84());

  public KmlIoFactory() {
    super(Kml22Constants.KML_FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.KML_MEDIA_TYPE, Kml22Constants.KML_FILE_EXTENSION);
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final KmlGeometryIterator iterator = new KmlGeometryIterator(resource);
    return new GeometryReader(iterator);
  }

  @Override
  public MapWriter createMapWriter(final java.io.Writer out) {
    return new KmlMapWriter(out);
  }

  @Override
  public MapWriter createMapWriter(final OutputStream out, final Charset charset) {
    return createMapWriter(out);
  }

  @Override
  public RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new KmlRecordWriter(writer);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return COORDINATE_SYSTEMS;
  }
}

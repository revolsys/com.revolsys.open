package com.revolsys.record.io.format.kml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.io.GeometryRecordReaderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class Kml extends GeometryRecordReaderFactory
  implements RecordWriterFactory, MapWriterFactory {

  public static final Set<CoordinateSystem> COORDINATE_SYSTEMS = Collections
    .singleton(EpsgCoordinateSystems.wgs84());

  public static long getLookAtRange(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty() || boundingBox.getWidth() == 0 && boundingBox.getHeight() == 0) {
      return 1000;
    } else {
      final double minX = boundingBox.getMinX();
      final double maxX = boundingBox.getMaxX();
      final double centreX = boundingBox.getCentreX();

      final double minY = boundingBox.getMinY();
      final double maxY = boundingBox.getMaxY();
      final double centreY = boundingBox.getCentreY();

      double maxMetres = 0;

      for (final double y : new double[] {
        minY, centreY, maxY
      }) {
        final double widthMetres = GeographicCoordinateSystem.distanceMetres(minX, y, maxX, y);
        if (widthMetres > maxMetres) {
          maxMetres = widthMetres;
        }
      }
      for (final double x : new double[] {
        minX, centreX, maxX
      }) {
        final double heightMetres = GeographicCoordinateSystem.distanceMetres(x, minY, x, maxY);
        if (heightMetres > maxMetres) {
          maxMetres = heightMetres;
        }
      }
      if (maxMetres == 0) {
        return 1000;
      } else {
        final double lookAtScale = 1.2;
        final double lookAtRange = maxMetres / 2 / Math.tan(Math.toRadians(25)) * lookAtScale;
        return (long)Math.ceil(lookAtRange);
      }
    }
  }

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
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new KmlRecordWriter(writer);
  }
}

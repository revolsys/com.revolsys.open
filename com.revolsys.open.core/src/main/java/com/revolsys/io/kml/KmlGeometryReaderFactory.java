package com.revolsys.io.kml;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.gis.geometry.io.AbstractGeometryReaderFactory;

public class KmlGeometryReaderFactory extends AbstractGeometryReaderFactory {

  public KmlGeometryReaderFactory() {
    super(Kml22Constants.FORMAT_DESCRIPTION, false);
    addMediaTypeAndFileExtension(Kml22Constants.MEDIA_TYPE,
      Kml22Constants.FILE_EXTENSION);
    setCoordinateSystems(EpsgCoordinateSystems.getCoordinateSystem(4326));
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final KmlGeometryIterator iterator = new KmlGeometryIterator(resource);
    return new GeometryReader(iterator);
  }

}

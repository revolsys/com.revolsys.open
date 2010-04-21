package com.revolsys.gis.kml.io;

import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.geometry.io.AbstractGeometryReaderFactory;
import com.vividsolutions.jts.geom.Geometry;

public class KmlGeometryReaderFacory extends AbstractGeometryReaderFactory {

  public KmlGeometryReaderFacory() {
    super(Kml22Constants.FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(Kml22Constants.MEDIA_TYPE,
      Kml22Constants.FILE_EXTENSION);
  }

  public Reader<Geometry> createGeometryReader(
    java.io.Reader in) {
    final KmlGeometryIterator iterator = new KmlGeometryIterator(in);
    return new IteratorReader<Geometry>(iterator);
  }

}

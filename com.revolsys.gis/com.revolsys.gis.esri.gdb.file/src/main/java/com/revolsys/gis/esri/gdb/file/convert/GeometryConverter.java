package com.revolsys.gis.esri.gdb.file.convert;

import com.vividsolutions.jts.geom.Envelope;

public class GeometryConverter {
  public static com.revolsys.gis.esri.gdb.file.swig.Envelope toEsri(
    Envelope envelope) {
    double xmin = envelope.getMinX();
    double ymin = envelope.getMinY();
    double xmax = envelope.getMaxX();
    double ymax = envelope.getMaxY();
    return new com.revolsys.gis.esri.gdb.file.swig.Envelope(xmin, xmax, ymin,
      ymax);
  }
}

package com.revolsys.gis.esri.gdb.file.convert;

import com.vividsolutions.jts.geom.Envelope;

public class GeometryConverter {
  public static com.revolsys.gis.esri.gdb.file.capi.swig.Envelope toEsri(
    final Envelope envelope) {
    final double xmin = envelope.getMinX();
    final double ymin = envelope.getMinY();
    final double xmax = envelope.getMaxX();
    final double ymax = envelope.getMaxY();
    return new com.revolsys.gis.esri.gdb.file.capi.swig.Envelope(xmin, xmax,
      ymin, ymax);
  }
}

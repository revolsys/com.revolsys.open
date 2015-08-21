package com.revolsys.gis.esri.gdb.file.convert;

import com.revolsys.geometry.model.BoundingBox;

public class GeometryConverter {
  public static com.revolsys.gis.esri.gdb.file.capi.swig.Envelope toEsri(
    final BoundingBox envelope) {
    final double xmin = envelope.getMinX();
    final double ymin = envelope.getMinY();
    final double xmax = envelope.getMaxX();
    final double ymax = envelope.getMaxY();
    return new com.revolsys.gis.esri.gdb.file.capi.swig.Envelope(xmin, xmax, ymin, ymax);
  }
}

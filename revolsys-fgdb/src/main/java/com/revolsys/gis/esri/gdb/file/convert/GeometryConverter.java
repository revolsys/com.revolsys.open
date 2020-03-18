package com.revolsys.gis.esri.gdb.file.convert;

import com.revolsys.geometry.model.BoundingBox;

public class GeometryConverter {
  public static com.revolsys.esri.filegdb.jni.Envelope toEsri(final BoundingBox envelope) {
    final double xmin = envelope.getMinX();
    final double ymin = envelope.getMinY();
    final double xmax = envelope.getMaxX();
    final double ymax = envelope.getMaxY();
    return new com.revolsys.esri.filegdb.jni.Envelope(xmin, xmax, ymin, ymax);
  }
}

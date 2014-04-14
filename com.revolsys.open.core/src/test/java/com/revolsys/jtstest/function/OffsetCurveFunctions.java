package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.operation.buffer.BufferParameters;
import com.revolsys.jts.operation.buffer.OffsetCurveBuilder;

public class OffsetCurveFunctions {

  public static Geometry offsetCurve(final Geometry geom, final double distance) {
    final BufferParameters bufParams = new BufferParameters();
    final OffsetCurveBuilder ocb = new OffsetCurveBuilder(
      geom.getGeometryFactory().getPrecisionModel(), bufParams);
    final Coordinates[] pts = ocb.getOffsetCurve(geom.getCoordinateArray(),
      distance);
    final Geometry curve = geom.getGeometryFactory().lineString(pts);
    return curve;
  }

}

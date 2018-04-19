package com.revolsys.geometry.cs.geoid;

import com.revolsys.geometry.cs.gridshift.VerticalShiftOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.model.BoundingBoxProxy;

public interface GeoidGrid extends BoundingBoxProxy {

  /**
   * Convert a geodetic (ellipsoid) height (h) to an orthometric (geoid) height (H).
   *
   * <pre>H = h - N</pre>
   * @param point The point to convert.
   * @return True if the point was converted.
   */
  boolean geodeticToOrthometricHeight(CoordinatesOperationPoint point);

  VerticalShiftOperation geodeticToOrthometricHeightOperation();

  /**
   * Get the height (N) of the geoid from the Ellipsoid.
   *
   * @param lon The point's longitude.
   * @param lon The point's latitude.
   */
  double getGeoidHeight(double lon, double lat);

  /**
   * Convert a orthometric (geoid) height (H) to an geodetic (ellipsoid) height(h).
   *
   * <pre>h = H - N</pre>
   * @param point The point to convert.
   * @return True if the point was converted.
   */
  boolean orthometricToGeodeticHeight(CoordinatesOperationPoint point);

  VerticalShiftOperation orthometricToGeodeticHeightOperation();

}

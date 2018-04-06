package com.revolsys.geometry.cs;

import org.junit.Test;

import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;

public class EllipsoidTest {
  private static final GeographicCoordinateSystem NAD83 = EpsgCoordinateSystems
    .getCoordinateSystem(4269);

  private static final Ellipsoid NAD83_ELLIPSOID = NAD83.getDatum().getEllipsoid();

  @Test
  public void testCartesianToGeodetic() {
    final CoordinatesOperationPoint point = new CoordinatesOperationPoint(-123, 49, 50);
    NAD83_ELLIPSOID.geodeticToCartesian(point);

  }
}

package com.revolsys.core.test.geometry.cs;

import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.systems.EpsgId;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;
import org.junit.Test;

public class EllipsoidTest {
  private static final GeographicCoordinateSystem NAD83 = EpsgCoordinateSystems
    .getCoordinateSystem(EpsgId.NAD83);

  private static final Ellipsoid NAD83_ELLIPSOID = NAD83.getDatum().getEllipsoid();

  @Test
  public void testCartesianToGeodetic() {
    final CoordinatesOperationPoint point = new CoordinatesOperationPoint(-123, 49, 50);
    NAD83_ELLIPSOID.geodeticToCartesian(point);

  }
}

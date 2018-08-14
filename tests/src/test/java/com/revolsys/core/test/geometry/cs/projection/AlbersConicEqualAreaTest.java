package com.revolsys.core.test.geometry.cs.projection;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.projection.AlbersConicEqualArea;
import com.revolsys.geometry.cs.projection.CoordinatesOperationPoint;
import com.revolsys.geometry.model.GeometryFactory;

public class AlbersConicEqualAreaTest {

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating2d(3005);

  private static final AlbersConicEqualArea PROJECTION = ((ProjectedCoordinateSystem)GEOMETRY_FACTORY
    .getHorizontalCoordinateSystem()).getCoordinatesProjection();

  private void assertProjection(final double x, final double y, final double lon,
    final double lat) {
    final CoordinatesOperationPoint point = new CoordinatesOperationPoint(x, y);
    PROJECTION.inverse(point);
    final double lonActual = point.x;
    final double latActual = point.y;
    Assert.assertEquals("lon", lon, lonActual, 1e-20);
    Assert.assertEquals("lat", lat, latActual, 1e-20);
    PROJECTION.project(point);
    final double xActual = point.x;
    final double yActual = point.y;
    Assert.assertEquals("x", x, xActual, 2e-3);
    Assert.assertEquals("y", y, yActual, 2e-3);
  }

  @Test
  public void test() {
    assertProjection(0, 0, -138.4458606948792, 44.199436504356555);
    assertProjection(1000000, 0, -126, 45);
    assertProjection(2000000, 0, -113.55413930512081, 44.199436504356555);
    assertProjection(0, 1000000, -141.06084626779327, 53.04658594392186);
    assertProjection(1000000, 1000000, -126, 54.0034889261628);
    assertProjection(2000000, 1000000, -110.93915373220675, 53.04658594392186);
    assertProjection(0, 2000000, -145.03356865639225, 61.769407557873485);
    assertProjection(1000000, 2000000, -126, 62.99422426226884);
    assertProjection(2000000, 2000000, -106.96643134360777, 61.769407557873485);
  }
}

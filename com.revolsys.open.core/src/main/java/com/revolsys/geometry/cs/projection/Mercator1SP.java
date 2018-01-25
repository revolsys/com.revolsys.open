package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.GeodeticDatum;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.ProjectionParameterNames;
import com.revolsys.geometry.cs.Spheroid;
import com.revolsys.math.Angle;

public class Mercator1SP extends AbstractCoordinatesProjection {

  private final double a;

  private final double e;

  private final double eOver2;

  private final double lambda0; // central meridian

  private final double x0;

  private final double y0;

  public Mercator1SP(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final GeodeticDatum geodeticDatum = geographicCS.getDatum();
    final double centralMeridian = cs
      .getDoubleParameter(ProjectionParameterNames.LONGITUDE_OF_CENTER);

    final Spheroid spheroid = geodeticDatum.getSpheroid();
    this.x0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_NORTHING);
    this.lambda0 = Math.toRadians(centralMeridian);
    this.a = spheroid.getSemiMajorAxis();
    this.e = spheroid.getEccentricity();
    this.eOver2 = this.e / 2;

  }

  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset) {
    final double dX = x - this.x0;
    final double dY = y - this.y0;

    final double lambda = dX / this.a + this.lambda0;

    final double t = Math.pow(Math.E, -dY / this.a);
    // TODO phi
    double phi = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {
      final double eSinPhi = this.e * Math.sin(phi);
      final double phi1 = Angle.PI_OVER_2
        - 2 * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.eOver2));
      delta = Math.abs(phi1 - phi);
      phi = phi1;
    } while (delta > 1.0e-011);

    targetCoordinates[targetOffset] = Math.toDegrees(lambda);
    targetCoordinates[targetOffset + 1] = Math.toDegrees(phi);
  }

  @Override
  public void project(final double lon, final double lat, final double[] targetCoordinates,
    final int targetOffset) {
    final double lambda = Math.toRadians(lon);
    final double phi = Math.toRadians(lat);

    final double x = this.a * (lambda - this.lambda0);

    final double eSinPhi = this.e * Math.sin(phi);
    final double y = this.a * Math.log(
      Math.tan(Angle.PI_OVER_4 + phi / 2) * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.eOver2));

    targetCoordinates[targetOffset] = x;
    targetCoordinates[targetOffset + 1] = y;
  }

}

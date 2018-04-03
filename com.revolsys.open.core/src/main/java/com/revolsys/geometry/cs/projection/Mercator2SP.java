package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.NormalizedParameterNames;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.math.Angle;

public class Mercator2SP extends AbstractCoordinatesProjection {

  private final double a;

  private final double e;

  private final double eOver2;

  private final double lambda0; // central meridian

  private final double multiple;

  private final double phi1;

  private final double x0;

  private final double y0;

  public Mercator2SP(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final GeodeticDatum geodeticDatum = geographicCS.getDatum();
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN);

    final Ellipsoid ellipsoid = geodeticDatum.getEllipsoid();
    this.x0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.lambda0 = Math.toRadians(centralMeridian);
    this.a = ellipsoid.getSemiMajorAxis();
    this.e = ellipsoid.getEccentricity();
    this.eOver2 = this.e / 2;
    this.phi1 = cs.getDoubleParameter(NormalizedParameterNames.STANDARD_PARALLEL_1);
    final double sinPhi1 = Math.sin(this.phi1);
    this.multiple = Math.cos(this.phi1) / Math.sqrt(1 - this.e * this.e * sinPhi1 * sinPhi1);
  }

  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset) {
    final double dX = (x - this.x0) / this.multiple;
    final double dY = (y - this.y0) / this.multiple;

    final double lambda = dX / this.a + this.lambda0;

    final double t = Math.pow(Math.E, -dY / this.a);
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

    final double x = this.a * (lambda - this.lambda0) * this.multiple;

    final double eSinPhi = this.e * Math.sin(phi);
    final double y = this.a
      * Math.log(
        Math.tan(Angle.PI_OVER_4 + phi / 2) * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.eOver2))
      * this.multiple;

    targetCoordinates[targetOffset] = this.x0 + x;
    targetCoordinates[targetOffset + 1] = this.y0 + y;
  }

}

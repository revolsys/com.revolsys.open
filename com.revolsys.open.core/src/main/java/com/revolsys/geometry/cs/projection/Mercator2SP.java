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

  private final double λ0; // central meridian

  private final double multiple;

  private final double φ1;

  private final double x0;

  private final double y0;

  public Mercator2SP(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final GeodeticDatum geodeticDatum = geographicCS.getDatum();
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN);

    final Ellipsoid ellipsoid = geodeticDatum.getEllipsoid();
    this.x0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λ0 = Math.toRadians(centralMeridian);
    this.a = ellipsoid.getSemiMajorAxis();
    this.e = ellipsoid.getEccentricity();
    this.eOver2 = this.e / 2;
    this.φ1 = cs.getDoubleParameter(NormalizedParameterNames.STANDARD_PARALLEL_1);
    final double sinPhi1 = Math.sin(this.φ1);
    this.multiple = Math.cos(this.φ1) / Math.sqrt(1 - this.e * this.e * sinPhi1 * sinPhi1);
  }

  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double dX = (point.x - this.x0) / this.multiple;
    final double dY = (point.y - this.y0) / this.multiple;

    final double λ = dX / this.a + this.λ0;

    final double t = Math.pow(Math.E, -dY / this.a);
    double φ = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {
      final double eSinPhi = this.e * Math.sin(φ);
      final double φ1 = Angle.PI_OVER_2
        - 2 * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.eOver2));
      delta = Math.abs(φ1 - φ);
      φ = φ1;
    } while (delta > 1.0e-011);

    point.x = λ;
    point.y = φ;
  }

  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;

    final double x = this.a * (λ - this.λ0) * this.multiple;

    final double eSinPhi = this.e * Math.sin(φ);
    final double y = this.a
      * Math.log(
        Math.tan(Angle.PI_OVER_4 + φ / 2) * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.eOver2))
      * this.multiple;

    point.x = this.x0 + x;
    point.y = this.y0 + y;
  }

}

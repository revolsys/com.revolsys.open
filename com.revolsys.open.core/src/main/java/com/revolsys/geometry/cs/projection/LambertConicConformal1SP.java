package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Datum;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.ProjectionParameterNames;
import com.revolsys.geometry.cs.Spheroid;
import com.revolsys.math.Angle;

public class LambertConicConformal1SP extends AbstractCoordinatesProjection {

  private final double a;

  private final double e;

  private final double ee;

  private final double f;

  /** The central origin. */
  private final double lambda0;

  private final double n;

  private final double rho0;

  private final double scaleFactor;

  private final double x0;

  private final double y0;

  public LambertConicConformal1SP(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final Datum datum = geographicCS.getDatum();
    this.scaleFactor = cs.getDoubleParameter(ProjectionParameterNames.SCALE_FACTOR);

    final Spheroid spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_NORTHING);

    final double longitudeOfNaturalOrigin = cs
      .getDoubleParameter(ProjectionParameterNames.LONGITUDE_OF_CENTER);
    this.lambda0 = Math.toRadians(longitudeOfNaturalOrigin);

    final double latitudeOfNaturalOrigin = cs
      .getDoubleParameter(ProjectionParameterNames.LATITUDE_OF_CENTER);
    final double phi0 = Math.toRadians(latitudeOfNaturalOrigin);

    this.a = spheroid.getSemiMajorAxis();
    this.e = spheroid.getEccentricity();
    this.ee = this.e * this.e;

    final double t0 = t(phi0);

    this.n = Math.sin(phi0);
    this.f = m(0) / (this.n * Math.pow(t(0), this.n));
    this.rho0 = this.a * this.f * Math.pow(t0, this.n);
  }

  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset) {
    double dX = x - this.x0;
    double dY = y - this.y0;

    double rho0 = this.rho0;
    if (this.n < 0) {
      rho0 = -rho0;
      dX = -dX;
      dY = -dY;
    }
    final double theta = Math.atan(dX / (rho0 - dY));
    double rho = Math.sqrt(dX * dX + Math.pow(rho0 - dY, 2));
    if (this.n < 0) {
      rho = -rho;
    }
    final double t = Math.pow(rho / (this.a * this.f * this.scaleFactor), 1 / this.n);
    double phi = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {

      final double sinPhi = Math.sin(phi);
      final double eSinPhi = this.e * sinPhi;
      final double phi1 = Angle.PI_OVER_2
        - 2 * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.e / 2));
      delta = Math.abs(phi1 - phi);
      phi = phi1;
    } while (!Double.isNaN(phi) && delta > 1.0e-011);
    final double lambda = theta / this.n + this.lambda0;

    targetCoordinates[targetOffset] = lambda;
    targetCoordinates[targetOffset + 1] = phi;
  }

  private double m(final double phi) {
    final double sinPhi = Math.sin(phi);
    return Math.cos(phi) / Math.sqrt(1 - this.ee * sinPhi * sinPhi);
  }

  @Override
  public void project(final double lambda, final double phi, final double[] targetCoordinates,
    final int targetOffset) {

    final double t = t(phi);
    final double rho = this.a * this.f * Math.pow(t, this.n) * this.scaleFactor;

    final double theta = this.n * (lambda - this.lambda0);
    final double x = this.x0 + rho * Math.sin(theta);
    final double y = this.y0 + this.rho0 - rho * Math.cos(theta);

    targetCoordinates[targetOffset] = x;
    targetCoordinates[targetOffset + 1] = y;
  }

  private double t(final double phi) {
    final double sinPhi = Math.sin(phi);
    final double eSinPhi = this.e * sinPhi;

    final double t = Math.tan(Angle.PI_OVER_4 - phi / 2)
      / Math.pow((1 - eSinPhi) / (1 + eSinPhi), this.e / 2);
    return t;
  }
}

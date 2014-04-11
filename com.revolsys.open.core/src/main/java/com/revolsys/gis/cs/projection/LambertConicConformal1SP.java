package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.jts.algorithm.Angle;
import com.revolsys.jts.geom.Coordinates;

public class LambertConicConformal1SP implements CoordinatesProjection {
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
    this.scaleFactor = cs.getDoubleParameter("scale_factor_at_natural_origin");

    final Spheroid spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter("false_easting");
    this.y0 = cs.getDoubleParameter("false_northing");

    final double longitudeOfNaturalOrigin = cs.getDoubleParameter("longitude_of_natural_origin");
    this.lambda0 = Math.toRadians(longitudeOfNaturalOrigin);

    final double latitudeOfNaturalOrigin = cs.getDoubleParameter("latitude_of_natural_origin");
    final double phi0 = Math.toRadians(latitudeOfNaturalOrigin);

    this.a = spheroid.getSemiMajorAxis();
    this.e = spheroid.getEccentricity();
    this.ee = e * e;

    final double t0 = t(phi0);

    this.n = Math.sin(phi0);
    this.f = m(0) / (n * Math.pow(t(0), n));
    this.rho0 = a * f * Math.pow(t0, n);
  }

  @Override
  public void inverse(final Coordinates from, final Coordinates to) {
    double x = from.getX() - x0;
    double y = from.getY() - y0;

    double rho0 = this.rho0;
    if (n < 0) {
      rho0 = -rho0;
      x = -x;
      y = -y;
    }
    final double theta = Math.atan(x / (rho0 - y));
    double rho = Math.sqrt(x * x + Math.pow(rho0 - y, 2));
    if (n < 0) {
      rho = -rho;
    }
    final double t = Math.pow(rho / (a * f * scaleFactor), 1 / n);
    double phi = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {

      final double sinPhi = Math.sin(phi);
      final double eSinPhi = e * sinPhi;
      final double phi1 = Angle.PI_OVER_2 - 2
        * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), e / 2));
      delta = Math.abs(phi1 - phi);
      phi = phi1;
    } while (!Double.isNaN(phi) && delta > 1.0e-011);
    final double lambda = theta / n + lambda0;

    to.setValue(0, lambda);
    to.setValue(1, phi);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  private double m(final double phi) {
    final double sinPhi = Math.sin(phi);
    return Math.cos(phi) / Math.sqrt(1 - ee * sinPhi * sinPhi);
  }

  @Override
  public void project(final Coordinates from, final Coordinates to) {
    final double lambda = from.getX();
    final double phi = from.getY();

    final double t = t(phi);
    final double rho = a * f * Math.pow(t, n) * scaleFactor;

    final double theta = n * (lambda - lambda0);
    final double x = x0 + rho * Math.sin(theta);
    final double y = y0 + rho0 - rho * Math.cos(theta);

    to.setValue(0, x);
    to.setValue(1, y);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  private double t(final double phi) {
    final double sinPhi = Math.sin(phi);
    final double eSinPhi = e * sinPhi;

    final double t = Math.tan(Angle.PI_OVER_4 - phi / 2)
      / Math.pow(((1 - eSinPhi) / (1 + eSinPhi)), e / 2);
    return t;
  }
}

package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.jts.algorithm.Angle;

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
    final Datum datum = geographicCS.getDatum();
    final double centralMeridian = cs.getDoubleParameter("longitude_of_natural_origin");

    final Spheroid spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter("false_easting");
    this.y0 = cs.getDoubleParameter("false_northing");
    this.lambda0 = Math.toRadians(centralMeridian);
    this.a = spheroid.getSemiMajorAxis();
    this.e = spheroid.getEccentricity();
    this.eOver2 = e / 2;
    this.phi1 = cs.getDoubleParameter("latitude_of_1st_standard_parallel");
    final double sinPhi1 = Math.sin(phi1);
    this.multiple = Math.cos(phi1) / Math.sqrt(1 - e * e * sinPhi1 * sinPhi1);
  }

  @Override
  public void inverse(final double x, final double y,
    final double[] targetCoordinates, final int targetOffset,
    final int targetNumAxis) {
    final double dX = (x - x0) / multiple;
    final double dY = (y - y0) / multiple;

    final double lambda = dX / a + lambda0;

    final double t = Math.pow(Math.E, -dY / a);
    double phi = Angle.PI_OVER_2 - 2 * Math.atan(t);
    double delta = 10e010;
    do {
      final double eSinPhi = e * Math.sin(phi);
      final double phi1 = Angle.PI_OVER_2 - 2
        * Math.atan(t * Math.pow((1 - eSinPhi) / (1 + eSinPhi), eOver2));
      delta = Math.abs(phi1 - phi);
      phi = phi1;
    } while (delta > 1.0e-011);

    targetCoordinates[targetOffset * targetNumAxis] = lambda;
    targetCoordinates[targetOffset * targetNumAxis + 1] = phi;
  }

  @Override
  public void project(final double lambda, final double phi,
    final double[] targetCoordinates, final int targetOffset,
    final int targetNumAxis) {

    final double x = (a * (lambda - lambda0)) * multiple;

    final double eSinPhi = e * Math.sin(phi);
    final double y = (a * Math.log(Math.tan(Angle.PI_OVER_4 + phi / 2)
      * Math.pow((1 - eSinPhi) / (1 + eSinPhi), eOver2)))
      * multiple;

    targetCoordinates[targetOffset * targetNumAxis] = x0 + x;
    targetCoordinates[targetOffset * targetNumAxis + 1] = y0 + y;
  }

}

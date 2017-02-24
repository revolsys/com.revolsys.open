package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Datum;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.ProjectionParameterNames;
import com.revolsys.geometry.cs.Spheroid;
import com.revolsys.math.Angle;

public class Mercator1SPSpherical extends AbstractCoordinatesProjection {
  /** The central origin. */
  private final double lambda0;

  private final double r;

  private final double x0;

  private final double y0;

  public Mercator1SPSpherical(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final Datum datum = geographicCS.getDatum();
    final double centralMeridian = cs
      .getDoubleParameter(ProjectionParameterNames.LONGITUDE_OF_CENTER);

    final Spheroid spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(ProjectionParameterNames.FALSE_NORTHING);
    this.lambda0 = Math.toRadians(centralMeridian);
    this.r = spheroid.getSemiMinorAxis();

  }

  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset) {
    final double dX = x - this.x0;
    final double dY = y - this.y0;

    final double lambda = dX / this.r + this.lambda0;

    final double phi = Angle.PI_OVER_2 - 2 * Math.atan(Math.pow(Math.E, -dY / this.r));

    targetCoordinates[targetOffset] = lambda;
    targetCoordinates[targetOffset + 1] = phi;
  }

  @Override
  public void project(final double lambda, final double phi, final double[] targetCoordinates,
    final int targetOffset) {
    final double x = this.r * (lambda - this.lambda0);

    final double y = this.r * Math.log(Math.tan(Angle.PI_OVER_4 + phi / 2));

    targetCoordinates[targetOffset] = this.x0 + x;
    targetCoordinates[targetOffset + 1] = this.y0 + y;
  }

}

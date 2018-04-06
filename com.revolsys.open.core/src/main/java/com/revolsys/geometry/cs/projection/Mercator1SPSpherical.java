package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.NormalizedParameterNames;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.math.Angle;

public class Mercator1SPSpherical extends AbstractCoordinatesProjection {
  /** The central origin. */
  private final double λ0;

  private final double r;

  private final double x0;

  private final double y0;

  public Mercator1SPSpherical(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geograφcCS = cs.getGeographicCoordinateSystem();
    final GeodeticDatum geodeticDatum = geograφcCS.getDatum();
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN);

    final Ellipsoid ellipsoid = geodeticDatum.getEllipsoid();
    this.x0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.y0 = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λ0 = Math.toRadians(centralMeridian);
    this.r = ellipsoid.getSemiMinorAxis();

  }

  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double dX = point.x - this.x0;
    final double dY = point.y - this.y0;

    final double r2 = this.r;
    final double λ = dX / r2 + this.λ0;
    final double φ = Angle.PI_OVER_2 - 2 * Math.atan(Math.pow(Math.E, -dY / r2));

    point.x = λ;
    point.y = φ;
  }

  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;

    final double r2 = this.r;
    point.x = this.x0 + r2 * (λ - this.λ0);
    point.y = this.y0 + r2 * Math.log(Math.tan(Angle.PI_OVER_4 + φ / 2));
  }

}

package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.NormalizedParameterNames;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.math.Angle;

public class WebMercator extends AbstractCoordinatesProjection {

  private final double xo;

  private final double yo;

  private final double λo;

  private final double a;

  public WebMercator(final ProjectedCoordinateSystem cs) {
    final double centralMeridian = cs.getDoubleParameter(NormalizedParameterNames.CENTRAL_MERIDIAN);
    this.xo = cs.getDoubleParameter(NormalizedParameterNames.FALSE_EASTING);
    this.yo = cs.getDoubleParameter(NormalizedParameterNames.FALSE_NORTHING);
    this.λo = Math.toRadians(centralMeridian);
    final Ellipsoid ellipsoid = cs.getGeographicCoordinateSystem().getDatum().getEllipsoid();
    this.a = ellipsoid.getSemiMajorAxis();
  }

  @Override
  public void inverse(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    final double a = this.a;
    point.x = this.λo + (x - this.xo) / a;
    point.y = Angle.PI_OVER_2 - 2 * Math.atan(Math.exp((this.yo - y) / a));
  }

  @Override
  public void project(final CoordinatesOperationPoint point) {
    final double λ = point.x;
    final double φ = point.y;
    final double a = this.a;
    point.x = this.xo + a * (λ - this.λo);
    point.y = this.yo + a * Math.log(Math.tan(Angle.PI_OVER_4 + φ / 2));
  }

}

package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.algorithm.Angle;

public class Mercator1SPSpherical implements CoordinatesProjection {
  /** The central origin. */
  private final double lambda0;

  private final double r;

  private final double x0;

  private final double y0;

  public Mercator1SPSpherical(final ProjectedCoordinateSystem cs) {
    final GeographicCoordinateSystem geographicCS = cs.getGeographicCoordinateSystem();
    final Datum datum = geographicCS.getDatum();
    final double centralMeridian = cs.getDoubleParameter("longitude_of_natural_origin");

    final Spheroid spheroid = datum.getSpheroid();
    this.x0 = cs.getDoubleParameter("false_easting");
    this.y0 = cs.getDoubleParameter("false_northing");
    this.lambda0 = Math.toRadians(centralMeridian);
    this.r = spheroid.getSemiMinorAxis();

  }

  public void inverse(final Coordinates from, final Coordinates to) {
    final double x = (from.getX() - x0);
    final double y = (from.getY() - y0);

    final double lambda = x / r + lambda0;

    final double phi = Angle.PI_OVER_2 - 2
      * Math.atan(Math.pow(Math.E, -y / r));

    to.setValue(0, lambda);
    to.setValue(1, phi);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  public void project(final Coordinates from, final Coordinates to) {
    final double lambda = from.getX();
    final double phi = from.getY();

    final double x = r * (lambda - lambda0);

    final double y = r * Math.log(Math.tan(Angle.PI_OVER_4 + phi / 2));

    to.setValue(0, x0 + x);
    to.setValue(1, y0 + y);

    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

}

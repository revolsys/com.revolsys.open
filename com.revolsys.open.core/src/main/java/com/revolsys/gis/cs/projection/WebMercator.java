package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.Datum;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Spheroid;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.algorithm.Angle;

public class WebMercator implements CoordinatesProjection {

  private final double a;

  private final double e;

  private final double eOver2;

  private final double lambda0; // central meridian

  private final double x0;

  private final double y0;

  public WebMercator(final ProjectedCoordinateSystem cs) {
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

  }

  @Override
  public void inverse(final Coordinates from, final Coordinates to) {
    final double x = from.getX();
    final double y = from.getY();

  // TODO radians
    
    double lon = (x / 20037508.34) * 180;
    double lat = (y / 20037508.34) * 180;

    lat = 180/Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
   
    to.setValue(0, lon);
    to.setValue(1, lat);
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  @Override
  public void project(final Coordinates from, final Coordinates to) {
    final double lon = from.getX();
    final double lat = from.getY();

    double x = lon * 20037508.34 / 180;
    double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
    y = y * 20037508.34 / 180;

    to.setValue(0, x);
    to.setValue(1, y);

    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

}

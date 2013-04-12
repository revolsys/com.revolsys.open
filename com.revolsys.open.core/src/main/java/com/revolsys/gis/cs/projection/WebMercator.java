package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.model.coordinates.Coordinates;

public class WebMercator implements CoordinatesProjection {

  public WebMercator(final ProjectedCoordinateSystem cs) {
  }

  @Override
  public void inverse(final Coordinates from, final Coordinates to) {
    final double x = from.getX();
    final double y = from.getY();

    final double lon = (x / 20037508.34) * 180;
    double lat = (y / 20037508.34) * 180;

    lat = 180 / Math.PI
      * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

    to.setValue(0, Math.toRadians(lon));
    to.setValue(1, Math.toRadians(lat));
    for (int i = 2; i < from.getNumAxis() && i < to.getNumAxis(); i++) {
      final double ordinate = from.getValue(i);
      to.setValue(i, ordinate);
    }
  }

  @Override
  public void project(final Coordinates from, final Coordinates to) {
    final double lon = Math.toDegrees(from.getX());
    final double lat = Math.toDegrees(from.getY());

    final double x = lon * 20037508.34 / 180;
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

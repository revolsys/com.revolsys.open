package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.ProjectedCoordinateSystem;

public class WebMercator extends AbstractCoordinatesProjection {

  public WebMercator(final ProjectedCoordinateSystem cs) {
  }

  @Override
  public void inverse(final double x, final double y, final double[] targetCoordinates,
    final int targetOffset, final int targetAxisCount) {
    final double lon = x / 20037508.34 * 180;
    double lat = y / 20037508.34 * 180;

    lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);

    targetCoordinates[targetOffset * targetAxisCount] = Math.toRadians(lon);
    targetCoordinates[targetOffset * targetAxisCount + 1] = Math.toRadians(lat);
  }

  @Override
  public void project(final double xDegrees, final double yDegrees,
    final double[] targetCoordinates, final int targetOffset, final int targetAxisCount) {
    final double lon = Math.toDegrees(xDegrees);
    final double lat = Math.toDegrees(yDegrees);

    final double x = lon * 20037508.34 / 180;
    double y = Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180);
    y = y * 20037508.34 / 180;
    targetCoordinates[targetOffset * targetAxisCount] = x;
    targetCoordinates[targetOffset * targetAxisCount + 1] = y;
  }

}

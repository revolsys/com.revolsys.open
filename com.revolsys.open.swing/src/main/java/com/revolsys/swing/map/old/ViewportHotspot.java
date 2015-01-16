package com.revolsys.swing.map.old;

import com.revolsys.jts.geom.Point;

public class ViewportHotspot {
  private final Point coordinate;

  private final String text;

  private final String url;

  public ViewportHotspot(final Point coordinate, final String text,
    final String url) {
    this.coordinate = coordinate;
    this.text = text;
    this.url = url;
  }

  public Point getCoordinate() {
    return this.coordinate;
  }

  public String getText() {
    return this.text;
  }

  public String getUrl() {
    return this.url;
  }
}

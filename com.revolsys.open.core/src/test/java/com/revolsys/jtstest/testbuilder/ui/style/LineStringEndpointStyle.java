package com.revolsys.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jtstest.testbuilder.Viewport;

public abstract class LineStringEndpointStyle extends LineStringStyle {
  private final boolean start;

  public LineStringEndpointStyle(final boolean start) {
    this.start = start;
  }

  private void paint(final Coordinates terminal, final Coordinates next,
    final Viewport viewport, final Graphics2D graphics) throws Exception {
    paint(
      viewport.toView(new Point2D.Double(terminal.getX(), terminal.getY())),
      viewport.toView(new Point2D.Double(next.getX(), next.getY())), viewport,
      graphics);
  }

  protected abstract void paint(Point2D terminal, Point2D next,
    Viewport viewport, Graphics2D graphics) throws Exception;

  @Override
  protected void paintLineString(final LineString lineString,
    final int lineType, final Viewport viewport, final Graphics2D graphics)
    throws Exception {
    if (lineString.isEmpty()) {
      return;
    }

    paint(
      start ? lineString.getCoordinateN(0)
        : lineString.getCoordinateN(lineString.getNumPoints() - 1),
      start ? lineString.getCoordinateN(1)
        : lineString.getCoordinateN(lineString.getNumPoints() - 2), viewport,
      graphics);
  }

}

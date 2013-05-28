package com.revolsys.swing.map.layer.dataobject.style.marker;

import java.awt.Graphics2D;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;

public abstract class AbstractMarker implements Marker {

  protected void translateMarker(final Viewport2D viewport,
    final Graphics2D graphics, final MarkerStyle style,double x, double y, final double width,
    final double height, double orientation) {

    graphics.translate(x, y);
    if (orientation != 0) {
      graphics.rotate(Math.toRadians(orientation));
    }

    final Measure<Length> deltaX = style.getMarkerDeltaX();
    final Measure<Length> deltaY = style.getMarkerDeltaY();
    double dx = Viewport2D.toDisplayValue(viewport, deltaX);
    double dy = Viewport2D.toDisplayValue(viewport, deltaY);

    final String verticalAlignment = style.getMarkerVerticalAlignment();
    if ("top".equals(verticalAlignment)) {
      dy -= height;
    } else if ("auto".equals(verticalAlignment)
      || "middle".equals(verticalAlignment)) {
      dy -= height / 2;
    }
    final String horizontalAlignment = style.getMarkerHorizontalAlignment();
    if ("right".equals(horizontalAlignment)) {
      dx -= width;
    } else if ("auto".equals(horizontalAlignment)
      || "center".equals(horizontalAlignment)) {
      dx -= width / 2;
    }

    graphics.translate(dx, dy);
  }
}

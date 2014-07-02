package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.swing.Icon;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;

public abstract class AbstractMarker implements Marker {

  @Override
  public Icon getIcon(MarkerStyle style) {
    return null;
  }

  protected void translateMarker(final Viewport2D viewport,
    final Graphics2D graphics, final MarkerStyle style, final double x,
    final double y, final double width, final double height,
    final double orientation) {
    if (viewport != null) {

      final double[] viewCoordinates = viewport.toViewCoordinates(x, y);
      graphics.translate(viewCoordinates[0], viewCoordinates[1]);
    }
    if (orientation != 0) {
      graphics.rotate(-Math.toRadians(orientation));
    }

    final Measure<Length> deltaX = style.getMarkerDx();
    final Measure<Length> deltaY = style.getMarkerDy();
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

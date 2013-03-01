package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import com.revolsys.swing.map.Viewport2D;

public class ImageMarker implements Marker {

  private Image image;

  public ImageMarker(final Image image) {
    this.image = image;
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY,
    final double orientation) {
    if (image != null) {
      final AffineTransform savedTransform = graphics.getTransform();
      final Measure<Length> markerWidth = style.getMarkerWidth();
      final double mapWidth = viewport.toDisplayValue(markerWidth);
      final Measure<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = viewport.toDisplayValue(markerHeight);

      graphics.translate(modelX, modelY);
      if (orientation != 0) {
        graphics.rotate(Math.toRadians(orientation));
      }

      final Measure<Length> deltaX = style.getMarkerDeltaX();
      final Measure<Length> deltaY = style.getMarkerDeltaY();
      double dx = viewport.toDisplayValue(deltaX);
      double dy = viewport.toDisplayValue(deltaY);

      final String verticalAlignment = style.getMarkerVerticalAlignment();
      if ("top".equals(verticalAlignment)) {
        dy -= mapHeight;
      } else if ("middle".equals(verticalAlignment)) {
        dy -= mapHeight / 2;
      }
      final String horizontalAlignment = style.getMarkerHorizontalAlignment();
      if ("right".equals(horizontalAlignment)) {
        dx -= mapWidth;
      } else if ("center".equals(horizontalAlignment)) {
        dx -= mapWidth / 2;
      }

      graphics.translate(dx, dy);

      final AffineTransform shapeTransform = AffineTransform.getScaleInstance(
        mapWidth / image.getWidth(null), mapHeight / image.getHeight(null));
      graphics.drawImage(image, shapeTransform, null);
      graphics.setTransform(savedTransform);
    }
  }
}

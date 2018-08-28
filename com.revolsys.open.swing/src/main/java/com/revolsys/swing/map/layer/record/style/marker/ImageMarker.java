package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.measure.Quantity;
import javax.measure.quantity.Length;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRender;

public class ImageMarker extends AbstractMarker {

  private Image image;

  public ImageMarker(final Image image) {
    this.image = image;
  }

  public ImageMarker(final Resource resource) {
    final InputStream in = resource.getInputStream();
    try {
      this.image = ImageIO.read(in);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to read file: " + resource);
    } finally {
      FileUtil.closeSilent(in);
    }
  }

  @Override
  public void render(final Graphics2DViewRender view, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY, double orientation) {
    if (this.image != null) {
      try (
        BaseCloseable closable = view.useViewCoordinates()) {
        final Quantity<Length> markerWidth = style.getMarkerWidth();
        final double mapWidth = view.toDisplayValue(markerWidth);
        final Quantity<Length> markerHeight = style.getMarkerHeight();
        final double mapHeight = view.toDisplayValue(markerHeight);

        final String orientationType = style.getMarkerOrientationType();
        if ("none".equals(orientationType)) {
          orientation = 0;
        }
        translateMarker(view, graphics, style, modelX, modelY, mapWidth, mapHeight, orientation);

        final AffineTransform shapeTransform = AffineTransform.getScaleInstance(
          mapWidth / this.image.getWidth(null), mapHeight / this.image.getHeight(null));
        graphics.drawImage(this.image, shapeTransform, null);
      }
    }
  }
}

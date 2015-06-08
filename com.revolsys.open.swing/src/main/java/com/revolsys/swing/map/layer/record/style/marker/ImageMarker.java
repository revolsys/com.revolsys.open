package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;

public class ImageMarker extends AbstractMarker {

  private Image image;

  public ImageMarker(final Image image) {
    this.image = image;
  }

  public ImageMarker(final Resource resource) {
    final InputStream in = SpringUtil.getInputStream(resource);
    try {
      this.image = ImageIO.read(in);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to read file: " + resource);
    } finally {
      FileUtil.closeSilent(in);
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics, final MarkerStyle style,
    final double modelX, final double modelY, double orientation) {
    if (this.image != null) {
      final AffineTransform savedTransform = graphics.getTransform();
      final Measure<Length> markerWidth = style.getMarkerWidth();
      final double mapWidth = Viewport2D.toDisplayValue(viewport, markerWidth);
      final Measure<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = Viewport2D.toDisplayValue(viewport, markerHeight);

      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }
      translateMarker(viewport, graphics, style, modelX, modelY, mapWidth, mapHeight, orientation);

      final AffineTransform shapeTransform = AffineTransform.getScaleInstance(
        mapWidth / this.image.getWidth(null), mapHeight / this.image.getHeight(null));
      graphics.drawImage(this.image, shapeTransform, null);
      graphics.setTransform(savedTransform);
    }
  }
}

package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.Graphic;
import com.revolsys.swing.map.symbolizer.ImageGraphic;

public class ImageGraphicSymbolRenderer {
  public void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final Graphic graphic,
    final ImageGraphic symbol,
    final double x,
    final double y) {
    final BufferedImage image = symbol.getImage();
    if (image != null) {
      final AffineTransform savedTransform = graphics.getTransform();
      double width = image.getWidth();
      double height = image.getHeight();
      final Measure<Length> size = graphic.getSize();
      if (size != null) {
        final double sizeValue = viewport.toDisplayValue(size);
        width = sizeValue * width / height;
        height = sizeValue;
      }
      double dx = -width * graphic.getAnchorX().doubleValue();
      dx += viewport.toDisplayValue(graphic.getDisplacementX());
      double dy = -height * (1 - graphic.getAnchorY().doubleValue());
      dy -= viewport.toDisplayValue(graphic.getDisplacementY());
      if (dx != 0 && dy != 0) {
        graphics.translate(dx, dy);
      }
      final double rotation = graphic.getRotation().doubleValue(SI.RADIAN);
      if (rotation != 0) {
        graphics.rotate(rotation, x, y);
      }
      final int opacity = graphic.getOpacity().intValue();
      if (opacity != 0) {
        final float[] scales = {
          1f, 1f, 1f, (float)opacity / 255
        };
        final float[] offsets = new float[4];
        new RescaleOp(scales, offsets, null);
      }
      graphics.drawImage(image, (int)x, (int)y, (int)width, (int)height, null);
      graphics.setTransform(savedTransform);
    }
  }
}

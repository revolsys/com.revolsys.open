package com.revolsys.swing.component;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Float;

import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.util.PaintUtils;

public class BusyLabelPainter extends BusyPainter {

  private static final Ellipse2D.Float[] SHAPES = {
    newShape(6, 0), newShape(10, 2), newShape(12, 6), newShape(10, 10), newShape(6, 12),
    newShape(2, 10), newShape(0, 6), newShape(2, 2)
  };

  private static Float newShape(final float x, final float y) {
    return new Ellipse2D.Float(x, y, 3, 3);
  }

  private final Color[] trailColors;

  public BusyLabelPainter() {
    super(16);
    final Color baseColor = getBaseColor();
    final Color highlightColor = getHighlightColor();
    final int trailLength = getTrailLength();
    this.trailColors = new Color[trailLength];
    for (int t = 0; t < trailLength; t++) {
      final float terp = 1 - (float)(trailLength - t) / (float)trailLength;
      this.trailColors[t] = PaintUtils.interpolate(baseColor, highlightColor, terp);
    }
  }

  private Color calcFrameColor(final int i) {
    final int frame = getFrame();
    final Color baseColor = getBaseColor();
    if (frame != -1) {
      final int pointCount = 8;
      final int trailLength = getTrailLength();
      for (int t = 0; t < trailLength; t++) {
        if (i == (frame - t + pointCount) % pointCount) {
          return this.trailColors[t];
        }
      }
    }
    return baseColor;
  }

  @Override
  protected void doPaint(final Graphics2D g, final Object t, final int width, final int height) {
    for (int i = 0; i < SHAPES.length; i++) {
      final Ellipse2D.Float shape = SHAPES[i];
      final Color color = calcFrameColor(i);
      g.setColor(color);
      g.fill(shape);
    }
  }
}

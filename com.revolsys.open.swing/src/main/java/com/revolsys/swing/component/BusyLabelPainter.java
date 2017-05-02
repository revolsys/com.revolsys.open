package com.revolsys.swing.component;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.util.PaintUtils;

public class BusyLabelPainter extends BusyPainter {
  public BusyLabelPainter() {
    super(16);
  }

  private Color calcFrameColor(final int i) {
    final int frame = getFrame();
    final Color baseColor = getBaseColor();
    if (frame == -1) {
      return baseColor;
    }

    final int trailLength = getTrailLength();
    for (int t = 0; t < trailLength; t++) {
      final int pointCount = getPoints();
      if (i == (frame - t + pointCount) % pointCount) {
        final float terp = 1 - (float)(trailLength - t) / (float)trailLength;
        final Color highlightColor = getHighlightColor();
        return PaintUtils.interpolate(baseColor, highlightColor, terp);
      }
    }
    return baseColor;
  }

  @Override
  protected void doPaint(final Graphics2D g, final Object t, final int width, final int height) {
    drawPoint(g, 0, 6, 0);
    drawPoint(g, 1, 10, 2);
    drawPoint(g, 2, 12, 6);
    drawPoint(g, 3, 10, 10);
    drawPoint(g, 4, 6, 12);
    drawPoint(g, 5, 2, 10);
    drawPoint(g, 6, 0, 6);
    drawPoint(g, 7, 2, 2);
  }

  private void drawPoint(final Graphics2D g, final int index, final int x, final int y) {
    final Color color = calcFrameColor(index);
    g.setColor(color);
    g.drawOval(x, y, 2, 2);
    g.fillOval(x, y, 2, 2);
  }
}

package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.gradient.LinearGradient;

public class ColorGradientPanel extends JPanel {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final ColorGradientGriddedElevationModelRasterizer rasterizer;

  public ColorGradientPanel(final ColorGradientGriddedElevationModelRasterizer rasterizer) {
    this.rasterizer = rasterizer;
  }

  @Override
  protected void paintComponent(final Graphics g) {
    final Graphics2D graphics = (Graphics2D)g;
    super.paintComponent(g);
    final LinearGradient gradient = this.rasterizer.getGradient();
    final double minValue = gradient.getValueMin();
    final double maxValue = gradient.getValueMax();
    final double range = maxValue - minValue;
    final int width = getWidth();
    final int height = getHeight();
    if (range == 0) {
      final int colorInt = gradient.getColorIntForValue(minValue);
      final Color color = new Color(colorInt);
      graphics.setPaint(color);
      g.fillRect(0, 0, width, height);
    } else {
      for (int i = 0; i < width; i++) {
        final double percent = i / (double)width;
        final double value = minValue + range * percent;
        final int colorInt = gradient.getColorIntForValue(value);
        final Color color = new Color(colorInt);
        graphics.setPaint(color);
        g.fillRect(i, 0, i, height);
      }
    }
  }
}

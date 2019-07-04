package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.BorderLayout;
import java.awt.Dimension;

import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.swing.component.ValueField;

public class ColorGradientField extends ValueField {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public ColorGradientField(final ColorGradientGriddedElevationModelRasterizer rasterizer) {
    super("gradient", rasterizer.getGradient());
    setLayout(new BorderLayout());
    final ColorGradientPanel gradientPanel = new ColorGradientPanel(rasterizer);
    gradientPanel.setMinimumSize(new Dimension(300, 30));
    add(gradientPanel, BorderLayout.CENTER);
  }
}

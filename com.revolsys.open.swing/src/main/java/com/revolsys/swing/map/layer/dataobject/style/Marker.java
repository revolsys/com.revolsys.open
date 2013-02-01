package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.Graphics2D;

import com.revolsys.swing.map.Viewport2D;

public interface Marker {
  void render(Viewport2D viewport, Graphics2D graphics, Style style,
    double modelX, double modelY);
}

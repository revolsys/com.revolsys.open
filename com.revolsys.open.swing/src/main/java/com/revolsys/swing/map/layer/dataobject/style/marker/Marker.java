package com.revolsys.swing.map.layer.dataobject.style.marker;

import java.awt.Graphics2D;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;

public interface Marker {
  void render(Viewport2D viewport, Graphics2D graphics, MarkerStyle style,
    double modelX, double modelY, double orientation);
}

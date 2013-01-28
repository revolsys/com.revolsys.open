package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;

import com.revolsys.swing.map.Viewport2D;

public interface LayerRenderer<T extends Layer> {

  void render(Viewport2D viewport, Graphics2D graphics, T layer);
}

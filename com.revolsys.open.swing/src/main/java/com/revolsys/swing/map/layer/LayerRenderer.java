package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.util.Map;

import com.revolsys.swing.map.Viewport2D;

public interface LayerRenderer<T extends Layer> {

  void render(Viewport2D viewport, Graphics2D graphics);

  <V> V getValue(String name);

  Map<String, Object> getAllDefaults();

  String getName();

  boolean isVisible();

  void setVisible(boolean visible);
}

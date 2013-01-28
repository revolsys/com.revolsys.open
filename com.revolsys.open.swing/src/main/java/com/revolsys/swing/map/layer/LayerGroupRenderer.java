package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.map.Viewport2D;

public class LayerGroupRenderer implements LayerRenderer<LayerGroup> {
  public LayerGroupRenderer() {
  }

  @Override
  public void render(final Viewport2D viewport, Graphics2D graphics, final LayerGroup layer) {
    if (layer.isVisible()) {
      viewport.setUseModelCoordinates(false, graphics);
      final List<Layer> layers = new ArrayList<Layer>(layer.getLayers());
      Collections.reverse(layers);

      for (final Layer childLayer : layers) {
        if (childLayer.isVisible()) {
          try {
            final LayerRenderer<Layer> renderer = childLayer.getRenderer();
            if (renderer != null) {
              renderer.render(viewport, graphics, childLayer);
            }
          } catch (final Throwable e) {
            LoggerFactory.getLogger(getClass()).error(
              "Error rendering layer: " + childLayer, e);
          }
          viewport.setUseModelCoordinates(false, graphics);
        }
      }
    }
  }

}

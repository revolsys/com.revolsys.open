package com.revolsys.swing.map.layer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.map.Viewport2D;

public class LayerGroupRenderer extends AbstractLayerRenderer<LayerGroup> {
  public LayerGroupRenderer(final LayerGroup layer) {
    super("group", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final LayerGroup layer) {
    if (layer.isVisible(viewport.getScale())) {
      final boolean saved = viewport.setUseModelCoordinates(false, graphics);
      try {
        final List<Layer> layers = new ArrayList<Layer>(layer.getLayers());
        Collections.reverse(layers);

        for (final Layer childLayer : layers) {
          viewport.setUseModelCoordinates(false, graphics);
          if (childLayer.isVisible(viewport.getScale())) {
            try {
              final LayerRenderer<Layer> renderer = childLayer.getRenderer();
              if (renderer != null) {
                renderer.render(viewport);
              }
            } catch (final Throwable e) {
              LoggerFactory.getLogger(getClass()).error(
                "Error rendering layer: " + childLayer, e);
            }
          }
        }

      } finally {
        viewport.setUseModelCoordinates(saved, graphics);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    return Collections.emptyMap();
  }

}

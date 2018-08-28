package com.revolsys.swing.map.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.view.ViewRenderer;

public class LayerGroupRenderer extends AbstractLayerRenderer<LayerGroup> {
  public LayerGroupRenderer(final LayerGroup layer) {
    super("group", layer);
  }

  @Override
  public void render(final ViewRenderer viewport, final LayerGroup layer) {
    final double scaleForVisible = viewport.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      final List<Layer> layers = new ArrayList<>(layer.getLayers());
      Collections.reverse(layers);

      for (final Layer childLayer : viewport.cancellable(layers)) {
        if (childLayer.isVisible(scaleForVisible)) {
          try {
            final LayerRenderer<Layer> renderer = childLayer.getRenderer();
            if (renderer != null) {
              renderer.render(viewport);
            }
          } catch (final Throwable e) {
            if (!viewport.isCancelled()) {
              Logs.error(this, "Error rendering layer: " + childLayer, e);
            }
          }
        }
      }
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }

}

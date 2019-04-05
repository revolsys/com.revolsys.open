package com.revolsys.swing.map.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.swing.map.view.ViewRenderer;

public class LayerGroupRenderer extends AbstractLayerRenderer<LayerGroup> {
  public LayerGroupRenderer(final LayerGroup layer) {
    super("group", layer);
  }

  @Override
  public void render(final ViewRenderer view, final LayerGroup layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      final List<Layer> layers = new ArrayList<>(layer.getLayers());
      Collections.reverse(layers);

      for (final Layer childLayer : view.cancellable(layers)) {
        if (childLayer.isVisible(scaleForVisible)) {
          try {
            final LayerRenderer<Layer> renderer = childLayer.getRenderer();
            if (renderer != null) {
              renderer.render(view);
            }
          } catch (final Throwable e) {
            if (!view.isCancelled()) {
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

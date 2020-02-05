package com.revolsys.swing.map.layer;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.view.ViewRenderer;

public class LayerGroupRenderer extends AbstractLayerRenderer<LayerGroup> {
  public LayerGroupRenderer(final LayerGroup layer) {
    super("group", "Group", null);
    setLayer(layer);
  }

  @Override
  public void render(final ViewRenderer view, final LayerGroup layer) {
    final double scaleForVisible = view.getScaleForVisible();
    if (layer.isVisible(scaleForVisible)) {
      layer.forEachReverse(view, (childLayer) -> {
        if (childLayer.isVisible(scaleForVisible)) {
          view.renderLayer(childLayer);
        }
      });
    }
  }

  @Override
  public JsonObject toMap() {
    return JsonObject.EMPTY;
  }

}

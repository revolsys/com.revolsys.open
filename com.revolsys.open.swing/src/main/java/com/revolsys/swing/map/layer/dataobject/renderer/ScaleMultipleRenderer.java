package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

/**
 * Use the first renderer which is visible at the current scale, ignore all
 * others. Changes when the scale changes.
 */
public class ScaleMultipleRenderer extends AbstractMultipleRenderer {

  private long lastScale = 0;

  private AbstractDataObjectLayerRenderer renderer;

  @SuppressWarnings("unchecked")
  public ScaleMultipleRenderer(final DataObjectLayer layer,
    LayerRenderer<?> parent, final Map<String, Object> style) {
    super("scaleStyle", layer, parent, style);
    List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
    final List<Map<String, Object>> scales = (List<Map<String, Object>>)style.get("scales");
    for (final Map<String, Object> scaleStyle : scales) {
      final AbstractDataObjectLayerRenderer renderer = AbstractDataObjectLayerRenderer.getRenderer(
        layer, this, scaleStyle);
      renderers.add(renderer);
    }
    setRenderers(renderers);
  }

  private AbstractDataObjectLayerRenderer getRenderer(final long scale) {
    lastScale = scale;
    for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
      if (renderer.isVisible(scale)) {
        return renderer;
      }
    }
    return null;
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final DataObjectLayer layer) {
    final long scale = (long)viewport.getScale();
    if (scale != lastScale) {
      renderer = getRenderer(scale);
    }
    if (renderer != null && renderer.isVisible(scale)) {
      renderer.render(viewport, graphics, layer);
    }
  }

  @Override
  protected void renderObjects(Viewport2D viewport, Graphics2D graphics,
    DataObjectLayer layer, List<DataObject> dataObjects) {
    final long scale = (long)viewport.getScale();
    if (scale != lastScale) {
      renderer = getRenderer(scale);
    }
    if (renderer != null && renderer.isVisible(scale)) {
      renderer.renderObjects(viewport, graphics, layer, dataObjects);
    }
  }
}

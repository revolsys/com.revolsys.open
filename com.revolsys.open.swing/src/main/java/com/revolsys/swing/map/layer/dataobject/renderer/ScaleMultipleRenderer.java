package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
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

  public ScaleMultipleRenderer(final DataObjectLayer layer,
    LayerRenderer<?> parent, final Map<String, Object> style) {
    super("scaleStyle", layer, parent, style);
  }

  private AbstractDataObjectLayerRenderer getRenderer(Viewport2D viewport) {
    final long scale = (long)viewport.getScale();
    if (scale == lastScale) {
      if (renderer.isVisible(scale)) {
        return renderer;
      } else {
        return null;
      }
    } else {
      lastScale = scale;
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(scale)) {
          return renderer;
        }
      }
      return null;
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final DataObjectLayer layer) {
    AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      renderer.render(viewport, graphics, layer);
    }
  }

  @Override
  // NOTE: Needed for multiple styles
  protected void renderObjects(Viewport2D viewport, Graphics2D graphics,
    DataObjectLayer layer, List<DataObject> dataObjects) {
    AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      renderer.renderObjects(viewport, graphics, layer, dataObjects);
    }
  }

 
  @Override
  // NOTE: Needed for filter styles
  protected void renderObject(Viewport2D viewport, Graphics2D graphics,
    BoundingBox visibleArea, DataObjectLayer layer, DataObject dataObject) {
    AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      super.renderObject(viewport, graphics, visibleArea, layer, dataObject);
    }
  }
}

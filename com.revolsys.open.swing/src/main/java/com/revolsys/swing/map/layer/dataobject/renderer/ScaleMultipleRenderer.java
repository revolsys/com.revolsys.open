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
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    super("scaleStyle", layer, parent, style);
  }

  private AbstractDataObjectLayerRenderer getRenderer(final Viewport2D viewport) {
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
  public boolean isVisible(final DataObject object) {
    if (super.isVisible(object)) {
      if (renderer != null) {
        return renderer.isVisible(object);
      }
    }
    return false;
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final DataObjectLayer layer) {
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      renderer.render(viewport, graphics, layer);
    }
  }

  @Override
  // NOTE: Needed for filter styles
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object) {
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      if (isVisible(object)) {
        renderer.renderObject(viewport, graphics, visibleArea, layer, object);
      }
    }
  }

  @Override
  // NOTE: Needed for multiple styles
  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<DataObject> objects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      for (final DataObject object : objects) {
        if (isVisible(object)) {
          renderer.renderObject(viewport, graphics, visibleArea, layer, object);
        }
      }
    }
  }
}

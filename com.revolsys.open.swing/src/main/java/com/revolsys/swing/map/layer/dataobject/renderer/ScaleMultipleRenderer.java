package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.ExceptionUtil;

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
    if (scale == this.lastScale) {
      if (this.renderer.isVisible(scale)) {
        return this.renderer;
      } else {
        return null;
      }
    } else {
      this.lastScale = scale;
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(scale)) {
          return renderer;
        }
      }
      return null;
    }
  }

  @Override
  public boolean isVisible(final LayerDataObject object) {
    if (super.isVisible() && super.isVisible(object)) {
      if (this.renderer != null) {
        return this.renderer.isVisible(object);
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
    final DataObjectLayer layer, final LayerDataObject object) {
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      if (isVisible(object)) {
        try {
          renderer.renderObject(viewport, graphics, visibleArea, layer, object);
        } catch (final Throwable e) {
          ExceptionUtil.log(getClass(), "Unabled to render " + layer.getName()
            + " #" + object.getIdString(), e);
        }
      }
    }
  }

  @Override
  // NOTE: Needed for multiple styles
  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<LayerDataObject> objects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    final AbstractDataObjectLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      for (final LayerDataObject object : objects) {
        if (isVisible(object)) {
          renderer.renderObject(viewport, graphics, visibleArea, layer, object);
        }
      }
    }
  }
}

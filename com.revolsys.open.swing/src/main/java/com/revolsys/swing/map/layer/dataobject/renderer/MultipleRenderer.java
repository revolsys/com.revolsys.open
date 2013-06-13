package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;

/**
 * Use all the specified renderers to render the layer. All features are
 * rendered using the first renderer, then the second etc.
 */
public class MultipleRenderer extends AbstractMultipleRenderer {

  public MultipleRenderer(final DataObjectLayer layer) {
    super("multipleStyle", layer);
  }

  public MultipleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> multipleStyle) {
    super("multipleStyle", layer, parent, multipleStyle);
  }

  public void addStyle(final GeometryStyle style) {
    final GeometryStyleRenderer renderer = new GeometryStyleRenderer(
      getLayer(), this, style);
    addRenderer(renderer);
  }

  @Override
  // Needed for filter styles
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final LayerDataObject object) {
    if (isVisible(object)) {
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        final long scale = (long)viewport.getScale();
        if (renderer.isVisible(scale)) {
          renderer.renderObject(viewport, graphics, visibleArea, layer, object);
        }
      }
    }
  }

  @Override
  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<LayerDataObject> objects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
      final long scale = (long)viewport.getScale();
      if (renderer.isVisible(scale)) {
        for (final LayerDataObject object : objects) {
          if (renderer.isVisible(object)) {
            renderer.renderObject(viewport, graphics, visibleArea, layer,
              object);
          }
        }
      }
    }
  }
}

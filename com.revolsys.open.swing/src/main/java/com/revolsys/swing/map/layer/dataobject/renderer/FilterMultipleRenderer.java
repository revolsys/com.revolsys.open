package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

/**
 * For each object render using the first renderer that matches the filter.
 */
public class FilterMultipleRenderer extends AbstractMultipleRenderer {

  public FilterMultipleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    super("filterStyle", layer, parent, style);
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final LayerDataObject object) {
    if (isVisible(object)) {
      final double scale = viewport.getScale();
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(object) && !layer.isHidden(object)) {
          if (renderer.isVisible(scale)) {
            renderer.renderObject(viewport, graphics, visibleArea, layer,
              object);
          }
          // Only render using the first match
          return;
        }
      }
    }
  }
}

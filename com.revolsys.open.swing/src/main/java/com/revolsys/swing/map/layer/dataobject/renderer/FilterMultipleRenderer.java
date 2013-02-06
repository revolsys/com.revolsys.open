package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

/**
 * For each object render using the first renderer that matches the filter.
 */
public class FilterMultipleRenderer extends AbstractMultipleRenderer {

  public FilterMultipleRenderer(final DataObjectLayer layer,
    LayerRenderer<?> parent, final Map<String, Object> style) {
    super("filterStyle", layer, parent, style);
  }

  @Override
  protected void renderObject(Viewport2D viewport, Graphics2D graphics,
    BoundingBox visibleArea, DataObjectLayer layer, DataObject dataObject) {
    double scale = viewport.getScale();
    for (AbstractDataObjectLayerRenderer renderer : getRenderers()) {
      if (renderer.isVisible(dataObject)) {
        if (renderer.isVisible(scale)) {
          renderer.renderObject(viewport, graphics, visibleArea, layer,
            dataObject);
        }
        // Only render using the first match
        return;
      }
    }
  }
}

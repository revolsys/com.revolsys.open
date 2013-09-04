package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.ExceptionUtil;

/**
 * For each object render using the first renderer that matches the filter.
 */
public class FilterMultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = SilkIconLoader.getIconWithBadge(
    "style_multiple", "filter");

  public FilterMultipleRenderer(final DataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    super("filterStyle", layer, parent, style);
    setIcon(ICON);
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final LayerDataObject object) {
    if (isVisible(object)) {
      final double scale = viewport.getScale();
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isFilterAccept(object)) {
          if (renderer.isVisible(object) && !layer.isHidden(object)) {
            if (renderer.isVisible(scale)) {
              try {
                renderer.renderObject(viewport, graphics, visibleArea, layer,
                  object);
              } catch (final Throwable e) {
                ExceptionUtil.log(
                  getClass(),
                  "Unabled to render " + layer.getName() + " #"
                    + object.getIdString(), e);
              }
            }
          }
          // Only render using the first match
          return;
        }
      }
    }
  }
}

package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.util.ExceptionUtil;

/**
 * Use all the specified renderers to render the layer. All features are
 * rendered using the first renderer, then the second etc.
 */
public class MultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = SilkIconLoader.getIcon("style_multiple");

  public MultipleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public MultipleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> multipleStyle) {
    super("multipleStyle", layer, parent, multipleStyle);
    setIcon(ICON);
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
    final AbstractDataObjectLayer layer, final LayerDataObject object) {
    if (isVisible(object)) {
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        final long scale = (long)viewport.getScale();
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
    }
  }

  @Override
  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final AbstractDataObjectLayer layer,
    final List<LayerDataObject> objects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
      final long scale = (long)viewport.getScale();
      if (renderer.isVisible(scale)) {
        for (final LayerDataObject object : objects) {
          if (isVisible(object) && renderer.isVisible(object)) {
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
      }
    }
  }
}

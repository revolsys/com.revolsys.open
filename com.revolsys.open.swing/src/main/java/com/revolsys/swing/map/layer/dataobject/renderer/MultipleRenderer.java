package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Use all the specified renderers to render the layer. All features are
 * rendered using the first renderer, then the second etc.
 */
public class MultipleRenderer extends AbstractMultipleRenderer {

  public MultipleRenderer(final DataObjectLayer layer, LayerRenderer<?> parent,
    final Map<String, Object> multipleStyle) {
    super("multipleStyle", layer, parent, multipleStyle);
    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> styles = (List<Map<String, Object>>)multipleStyle.get("styles");
    for (final Map<String, Object> style : styles) {
      final AbstractDataObjectLayerRenderer renderer = AbstractDataObjectLayerRenderer.getRenderer(
        layer, this, style);
      addRenderer(renderer);
    }
  }

  public void addStyle(final GeometryStyle style) {
    final GeometryStyleRenderer renderer = new GeometryStyleRenderer(
      getLayer(), this, style);
    addRenderer(renderer);
  }

  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object,
    final GeometryStyle style) {
    final Geometry geometry = object.getGeometryValue();
    GeometryRendererUtil.renderGeometry(viewport, graphics, geometry, style);
  }

  @Override
  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<DataObject> dataObjects) {
    for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
      long scale = (long)viewport.getScale();
      if (renderer.isVisible(scale)) {
        renderer.renderObjects(viewport, graphics, layer, dataObjects);
      }
    }
  }

  public void setStyles(final List<GeometryStyle> styles) {
    List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
    for (final GeometryStyle style : styles) {
      renderers.add(new GeometryStyleRenderer(getLayer(), this, style));
    }
    setRenderers(renderers);
  }
}

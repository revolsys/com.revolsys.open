package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.Style;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

// TODO events
public class MultipleStyleRenderer extends AbstractDataObjectLayerRenderer {

  private List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();

  public MultipleStyleRenderer() {
  }

  public MultipleStyleRenderer(final List<Style> styles) {
    setStyles(styles);
  }

  public MultipleStyleRenderer(final Map<String, Object> multipleStyle) {
    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> styles = (List<Map<String, Object>>)multipleStyle.get("styles");
    for (final Map<String, Object> style : styles) {
      final AbstractDataObjectLayerRenderer renderer = AbstractDataObjectLayerRenderer.getRenderer(style);
      addRenderer(renderer);
    }
  }

  public MultipleStyleRenderer(final Style... styles) {
    this(Arrays.asList(styles));
  }

  public void addRenderer(final AbstractDataObjectLayerRenderer renderer) {
    if (renderer != null) {
      renderers.add(renderer);
    }
  }

  public void addStyle(final Style style) {
    final StyleRenderer renderer = new StyleRenderer(style);
    addRenderer(renderer);
  }

  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object, final Style style) {
    final Geometry geometry = object.getGeometryValue();
    GeometryRendererUtil.renderGeometry(viewport, graphics, geometry, style);
  }

  @Override
  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final DataObjectLayer layer,
    final List<DataObject> dataObjects) {
    for (final AbstractDataObjectLayerRenderer renderer : renderers) {
      renderer.renderObjects(viewport, graphics, layer, dataObjects);
    }
  }

  public void setRenderers(
    final List<? extends AbstractDataObjectLayerRenderer> renderers) {
    this.renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
  }

  public void setStyles(final List<Style> styles) {
    this.renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
    for (final Style style : styles) {
      renderers.add(new StyleRenderer(style));
    }
  }

}

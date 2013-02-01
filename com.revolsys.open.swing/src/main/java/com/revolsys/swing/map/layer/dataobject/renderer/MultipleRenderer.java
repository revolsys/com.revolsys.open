package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

// TODO events
public class MultipleRenderer extends AbstractDataObjectLayerRenderer {

  private List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();

  public MultipleRenderer() {
  }

  public MultipleRenderer(final GeometryStyle... styles) {
    this(Arrays.asList(styles));
  }

  public MultipleRenderer(final List<GeometryStyle> styles) {
    setStyles(styles);
  }

  public MultipleRenderer(final Map<String, Object> defaults,
    final Map<String, Object> multipleStyle) {
    super(defaults, multipleStyle);
    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> styles = (List<Map<String, Object>>)multipleStyle.get("styles");
    for (final Map<String, Object> style : styles) {
      final AbstractDataObjectLayerRenderer renderer = AbstractDataObjectLayerRenderer.getRenderer(
        getDefaults(), style);
      addRenderer(renderer);
    }
  }

  public void addRenderer(final AbstractDataObjectLayerRenderer renderer) {
    if (renderer != null) {
      renderers.add(renderer);
    }
  }

  public void addStyle(final GeometryStyle style) {
    final GeometryStyleRenderer renderer = new GeometryStyleRenderer(style);
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
    for (final AbstractDataObjectLayerRenderer renderer : renderers) {
      renderer.renderObjects(viewport, graphics, layer, dataObjects);
    }
  }

  public void setRenderers(
    final List<? extends AbstractDataObjectLayerRenderer> renderers) {
    this.renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
  }

  public void setStyles(final List<GeometryStyle> styles) {
    this.renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
    for (final GeometryStyle style : styles) {
      renderers.add(new GeometryStyleRenderer(style));
    }
  }
  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "multipleStyle");
    Map<String, Object> defaults = getDefaults();
    if (!defaults.isEmpty()) {
      map.put("defaults", defaults);
    }
    if (!renderers.isEmpty()) {
      map.put("styles"  , renderers);
    }
    return map;
  }
}

package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryStyleRenderer extends AbstractDataObjectLayerRenderer {

  private GeometryStyle style;

  public GeometryStyleRenderer() {
    this(new GeometryStyle());
  }

  public GeometryStyleRenderer(final GeometryStyle style) {
    this.style = style;
  }

  public GeometryStyleRenderer(Map<String, Object> defaults,
    Map<String, Object> geometryStyle) {
    super(defaults, geometryStyle);
    Map<String, Object> style = new HashMap<String, Object>(getDefaults());
    style.putAll(geometryStyle);
    this.style = new GeometryStyle(style);
  }

  public GeometryStyle getStyle() {
    return style;
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    GeometryRendererUtil.renderGeometry(viewport, graphics, geometry, style);
  }

  public void setStyle(final GeometryStyle style) {
    this.style = style;
  }
  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "geometryStyle");
    // TODO style properties
    return map;
  }
}

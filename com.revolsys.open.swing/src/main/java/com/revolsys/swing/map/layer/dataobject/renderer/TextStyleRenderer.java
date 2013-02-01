package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.TextStyle;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

public class TextStyleRenderer extends AbstractDataObjectLayerRenderer {

  private TextStyle style;

  public TextStyleRenderer() {
    this(new TextStyle());
  }

  public TextStyleRenderer(final TextStyle style) {
    this.style = style;
  }

  public TextStyleRenderer(Map<String, Object> defaults,
    Map<String, Object> textStyle) {
    super(defaults, textStyle);
    Map<String, Object> style = new HashMap<String, Object>(getDefaults());
    style.putAll(textStyle);
    this.style = new TextStyle(style);
  }


  public TextStyle getStyle() {
    return style;
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    GeometryRendererUtil.renderText(viewport, graphics,object, geometry, style);
  }

  public void setStyle(final TextStyle style) {
    this.style = style;
  }
  
  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "textStyle");
    // TODO style properties
    return map;
  }
}

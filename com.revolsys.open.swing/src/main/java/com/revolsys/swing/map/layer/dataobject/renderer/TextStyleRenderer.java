package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.TextStyle;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

public class TextStyleRenderer extends AbstractDataObjectLayerRenderer {

  private TextStyle style;


  public TextStyleRenderer(final DataObjectLayer layer, LayerRenderer<?> parent,
    final Map<String, Object> textStyle) {
    super("textStyle", layer,parent, textStyle);
    final Map<String, Object> style = getAllDefaults();
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
    GeometryRendererUtil.renderText(viewport, graphics, object, geometry, style);
  }

  public void setStyle(final TextStyle style) {
    this.style = style;
  }

}

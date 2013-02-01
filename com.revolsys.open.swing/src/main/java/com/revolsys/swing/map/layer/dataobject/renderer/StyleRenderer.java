package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.Style;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

public class StyleRenderer extends AbstractDataObjectLayerRenderer {

  private Style style;

  public StyleRenderer() {
    this(new Style());
  }

  public StyleRenderer(final Style style) {
    this.style = style;
  }

  public StyleRenderer(Map<String, Object> style) {
    this(new Style(style));
  }

  public Style getStyle() {
    return style;
  }

  @Override
  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final DataObjectLayer layer, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    GeometryRendererUtil.renderGeometry(viewport, graphics, geometry, style);
  }

  public void setStyle(final Style style) {
    this.style = style;
  }
}

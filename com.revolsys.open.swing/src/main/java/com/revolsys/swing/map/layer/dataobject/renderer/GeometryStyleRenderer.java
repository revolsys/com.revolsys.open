package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.geometry.GeometryRendererUtil;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryStyleRenderer extends AbstractDataObjectLayerRenderer {

  private GeometryStyle style;

  public GeometryStyleRenderer(final DataObjectLayer layer) {
    this(layer,new GeometryStyle());
  }

  public GeometryStyleRenderer(final DataObjectLayer layer,final GeometryStyle style) {
    this(layer,null,style);
  }
  public GeometryStyleRenderer(final DataObjectLayer layer,final LayerRenderer<?> parent,final GeometryStyle style) {
    super("geometryStyle", layer,parent);
    this.style = style;
  }

  public GeometryStyleRenderer(final DataObjectLayer layer, LayerRenderer<?> parent,
    final Map<String, Object> geometryStyle) {
    super("geometryStyle", layer,parent, geometryStyle);
    final Map<String, Object> style = getAllDefaults();
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

}

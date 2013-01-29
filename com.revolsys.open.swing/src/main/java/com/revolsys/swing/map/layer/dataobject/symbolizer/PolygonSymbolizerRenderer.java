package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.Fill;
import com.revolsys.swing.map.symbolizer.PolygonSymbolizer;
import com.revolsys.swing.map.symbolizer.Stroke;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonSymbolizerRenderer extends
  AbstractGeometrySymbolizerRenderer<PolygonSymbolizer> {
  public PolygonSymbolizerRenderer() {
    super(true);
  }

  @Override
  protected void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final PolygonSymbolizer style,
    final DataObject dataObject,
    final Geometry geometry) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        render(viewport, graphics, style, polygon);
      }
    }
  }

  public void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final PolygonSymbolizer style,
    final Polygon polygon) {
    final Stroke stroke = style.getStroke();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);
    final LineString newLine;
    // TODO 1/2 buffer line

    final Shape shape = GeometryShapeUtil.toShape(viewport, polygon);
    // TODO deal with displacement and rotation

    final Fill fill = style.getFill();
    if (fill != null) {
      SymbolizerUtil.setFill(graphics, fill);
      graphics.fill(shape);
    }
    if (stroke != null) {
      SymbolizerUtil.setStroke(viewport, graphics, stroke);
      graphics.draw(shape);
    }

  }

}

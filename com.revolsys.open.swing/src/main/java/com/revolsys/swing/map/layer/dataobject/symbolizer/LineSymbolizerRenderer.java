package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.LineSymbolizer;
import com.revolsys.swing.map.symbolizer.Stroke;
import com.revolsys.swing.map.util.GeometryShapeUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineSymbolizerRenderer extends
  AbstractGeometrySymbolizerRenderer<LineSymbolizer> {
  public LineSymbolizerRenderer() {
    super(true);
  }

  @Override
  protected void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final LineSymbolizer style,
    final DataObject dataObject,
    final Geometry geometry) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (part instanceof LineString) {
        final LineString line = (LineString)part;
        render(viewport, graphics, style, line);
      }
    }
  }

  protected void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final LineSymbolizer style,
    final LineString line) {
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    // TODO 1/2 buffer line
    final Shape shape = GeometryShapeUtil.toShape(viewport, line);
    // TODO deal with displacement and rotation
    final Stroke stroke = style.getStroke();
    if (stroke != null) {
      SymbolizerUtil.setStroke(viewport, graphics, stroke);
      graphics.draw(shape);
    }

  }
}

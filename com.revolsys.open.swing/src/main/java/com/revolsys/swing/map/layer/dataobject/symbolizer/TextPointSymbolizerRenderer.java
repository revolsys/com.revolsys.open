package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

import javax.measure.unit.NonSI;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.CssUtil;
import com.revolsys.swing.map.symbolizer.TextPointSymbolizer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class TextPointSymbolizerRenderer extends
  AbstractTextSymbolizerRenderer<TextPointSymbolizer> {

  @Override
  protected void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final TextPointSymbolizer symbolizer,
    final DataObject dataObject,
    final Geometry geometry) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometryN(i);
      if (part instanceof Point) {
        final Point point = (Point)part;
        render(viewport, graphics, symbolizer, dataObject, point);
      }
    }
  }

  protected void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final TextPointSymbolizer symbolizer,
    final DataObject dataObject,
    final Point point) {
    final String label = dataObject.getValue(symbolizer.getLabelPropertyName());
    if (label != null) {
      final double scaleFactor = 1.0 / viewport.getModelUnitsPerViewUnit();
      final Coordinate coordinate = point.getCoordinate();
      final double[] location = viewport.toViewCoordinates(coordinate.x,
        coordinate.y);
      final double x = location[0];
      final double y = location[1];
      final AffineTransform savedTransform = graphics.getTransform();
      graphics.translate(x, y);
      graphics.scale(scaleFactor, scaleFactor);

      double rotation = symbolizer.getRotation()
        .doubleValue(NonSI.DEGREE_ANGLE);
      rotation = rotation - Math.PI * 3;

      rotation = (450 - rotation) % 360;
      if (rotation != 0) {
        graphics.rotate(Math.toRadians(-rotation), 0, 0);
      }

      final double width = graphics.getFontMetrics().stringWidth(label);
      final double height = graphics.getFontMetrics().getAscent();
      graphics.translate(-width * symbolizer.getAnchorX().doubleValue(), height
        * symbolizer.getAnchorY().doubleValue());
      graphics.translate(
        -viewport.toDisplayValue(symbolizer.getDisplacementX()),
        viewport.toDisplayValue(symbolizer.getDisplacementY()));

      final double outlineRadius = viewport.toDisplayValue(symbolizer.getOutlineRadius());
      if (outlineRadius > 0) {
        final Stroke savedStroke = graphics.getStroke();
        final Stroke outlineStroke = new BasicStroke((int)outlineRadius,
          BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        graphics.setColor(CssUtil.getColor(symbolizer.getOutlineColor()));
        graphics.setStroke(outlineStroke);

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        final TextLayout textLayout = new TextLayout(label, graphics.getFont(),
          graphics.getFontRenderContext());

        graphics.draw(textLayout.getOutline(AffineTransform.getTranslateInstance(
          0, 0)));
        graphics.setStroke(savedStroke);
      }
      graphics.setColor(CssUtil.getColor(symbolizer.getColor()));
      graphics.drawString(label, (float)0, (float)0);
      graphics.setTransform(savedTransform);
    }
  }
}

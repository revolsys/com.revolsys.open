package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.Fill;
import com.revolsys.swing.map.symbolizer.Graphic;
import com.revolsys.swing.map.symbolizer.Stroke;
import com.revolsys.swing.map.symbolizer.WellKnownMarkGraphicSymbol;

public class WellKnownMarkGraphicSymbolRenderer {

  private static final Map<String, Shape> SHAPES = new HashMap<String, Shape>();

  static {
    SHAPES.put("square", square(1));
    SHAPES.put("circle", circle(1));
    SHAPES.put("triangle", triangle(1));
    SHAPES.put("star", star(1));
    SHAPES.put("cross", cross(1));
    SHAPES.put("x", x(1));
  }

  public static Shape circle(final double size) {
    return new Ellipse2D.Double(0, 0, size, size);
  }

  public static Shape cross(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size / 3, size);
    path.lineTo(size * 2 / 3, size);
    path.lineTo(size * 2 / 3, size * 2 / 3);
    path.lineTo(size, size * 2 / 3);
    path.lineTo(size, size / 3);
    path.lineTo(size * 2 / 3, size / 3);
    path.lineTo(size * 2 / 3, 0);
    path.lineTo(size / 3, 0);
    path.lineTo(size / 3, size / 3);
    path.lineTo(0, size / 3);
    path.lineTo(0, size * 2 / 3);
    path.lineTo(size / 3, size * 2 / 3);
    path.closePath();
    return path;
  }

  public static Shape square(final double size) {
    return new Rectangle2D.Double(0, 0, size, size);
  }

  public static Shape star(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size / 2, 0);
    path.lineTo(size * 0.64, size / 3);
    path.lineTo(size, size / 3);
    path.lineTo(size * .7, size * .57);
    path.lineTo(size * .8, size * .9);
    path.lineTo(size / 2, size * .683);
    path.lineTo(size * .2, size * .9);
    path.lineTo(size * .3, size * .57);
    path.lineTo(0, size / 3);
    path.lineTo(size * 0.36, size / 3);
    path.closePath();
    return path;
  }

  public static Shape triangle(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, size);
    path.lineTo(size / 2, 0);
    path.lineTo(size, size);
    path.closePath();
    return path;
  }

  /**
   * Get an X shape for the size of the graphic.
   * 
   * @return The shape.
   */
  public static Shape x(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size * .25, size);
    path.lineTo(size * .5, size * .75);
    path.lineTo(size * .75, size);
    path.lineTo(size, size * .75);
    path.lineTo(size * .75, size * .5);
    path.lineTo(size, size * .25);
    path.lineTo(size * .75, 0);
    path.lineTo(size * .5, size * .25);
    path.lineTo(size * .25, 0);
    path.lineTo(0, size * .25);
    path.lineTo(size * .25, size * .5);
    path.lineTo(0, size * .75);
    path.closePath();
    return path;
  }

  public void render(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final Graphic graphic,
    final WellKnownMarkGraphicSymbol symbol,
    final double originX,
    final double originY) {
    final Shape shape = SHAPES.get(symbol.getName());
    if (shape != null) {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
      final AffineTransform savedTransform = graphics.getTransform();
      Measure<Length> size = graphic.getSize();
      if (size == null) {
        size = Measure.valueOf(6.0, NonSI.PIXEL);
      }
      final double width = viewport.toDisplayValue(size);
      final double height = width;
      double x = originX;
      double y = originY;
      x -= width * graphic.getAnchorX().doubleValue();
      y -= height * (1 - graphic.getAnchorY().doubleValue());
      final double rotation = graphic.getRotation().doubleValue(SI.RADIAN);
      if (rotation != 0) {
        graphics.rotate(rotation, x, y);
      }
      x += viewport.toDisplayValue(graphic.getDisplacementX());
      y -= viewport.toDisplayValue(graphic.getDisplacementY());
      graphics.translate(x, y);
      // TODO see if we can cache these
      final Shape newShape = new GeneralPath(shape).createTransformedShape(AffineTransform.getScaleInstance(
        width, height));
      final Fill fill = symbol.getFill();
      if (fill != null) {
        SymbolizerUtil.setFill(graphics, fill);
        graphics.fill(newShape);
      }
      final Stroke stroke = symbol.getStroke();
      if (stroke != null) {
        SymbolizerUtil.setStroke(viewport, graphics, stroke);
        graphics.draw(newShape);
      }
      graphics.setTransform(savedTransform);
    }
  }

}

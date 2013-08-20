package com.revolsys.swing.map.layer.dataobject.style.marker;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;

public class ShapeMarker extends AbstractMarker {

  private static final Map<String, Shape> SHAPES = new HashMap<String, Shape>();

  static {
    SHAPES.put("square", square(1));
    SHAPES.put("rectangle", square(1));
    SHAPES.put("circle", circle(1));
    SHAPES.put("ellipse", circle(1));
    SHAPES.put("triangle", triangle(1));
    SHAPES.put("star", star(1));
    SHAPES.put("cross", cross(1));
    SHAPES.put("x", x(1));
    SHAPES.put("arrow", arrow(1));
    SHAPES.put("solidArrow", solidArrow(1));
    SHAPES.put("diamond", diamond(1));
  }

  /**
   * Get an arrow shape pointing right for the size of the graphic.
   * 
   * @return The shape.
   */
  public static Shape arrow(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, size);
    path.lineTo(size, size * .5);
    path.lineTo(0, 0);
    return path;
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

  public static Shape diamond(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size / 2, 0);
    path.lineTo(size, size / 2);
    path.lineTo(size / 2, size);
    path.lineTo(0, size / 2);
    path.closePath();
    return path;
  }

  /**
   * Get a solid arrow shape pointing right for the size of the graphic.
   * 
   * @return The shape.
   */
  public static Shape solidArrow(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, size);
    path.lineTo(size, size * .5);
    path.lineTo(0, 0);
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

  private String name;

  private Shape shape;

  public ShapeMarker(final Shape shape) {
    this.shape = shape;
    if (shape != null) {
      final Rectangle bounds = shape.getBounds();
      final double width = bounds.width;
      final double height = bounds.height;
      double scale;
      if (width > height) {
        scale = 1 / width;
      } else {
        scale = 1 / height;
      }
      final AffineTransform transform = AffineTransform.getTranslateInstance(
        -bounds.x, -bounds.y);
      transform.concatenate(AffineTransform.getScaleInstance(scale, scale));
      this.shape = new GeneralPath(shape).createTransformedShape(transform);
    }
  }

  public ShapeMarker(final String name) {
    this(SHAPES.get(name));
    this.name = name;
    if (this.shape == null) {
      throw new IllegalArgumentException("Unknown shape " + name);
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY,
    double orientation) {

    final AffineTransform savedTransform = graphics.getTransform();
    final Measure<Length> markerWidth = style.getMarkerWidth();
    final double mapWidth = Viewport2D.toDisplayValue(viewport, markerWidth);
    final Measure<Length> markerHeight = style.getMarkerHeight();
    final double mapHeight = Viewport2D.toDisplayValue(viewport, markerHeight);
    final String orientationType = style.getMarkerOrientationType();
    if ("none".equals(orientationType)) {
      orientation = 0;
    }

    translateMarker(viewport, graphics, style, modelX, modelY, mapWidth,
      mapHeight, orientation);

    final AffineTransform shapeTransform = AffineTransform.getScaleInstance(
      mapWidth, mapHeight);
    final Shape newShape = new GeneralPath(this.shape).createTransformedShape(shapeTransform);
    if (style.setMarkerFillStyle(viewport, graphics)) {
      graphics.fill(newShape);
    }
    if (style.setMarkerLineStyle(viewport, graphics)) {
      graphics.draw(newShape);
    }
    graphics.setTransform(savedTransform);
  }

  @Override
  public String toString() {
    if (this.name == null) {
      return "unknown";
    } else {
      return this.name;
    }
  }
}

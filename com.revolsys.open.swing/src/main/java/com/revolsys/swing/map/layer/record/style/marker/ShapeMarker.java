package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.util.Property;

public class ShapeMarker extends AbstractMarker {

  private static final Map<String, Shape> SHAPES = new TreeMap<String, Shape>();

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

  public static List<Marker> getMarkers() {
    final List<Marker> markers = new ArrayList<Marker>();
    for (final String markerName : SHAPES.keySet()) {
      final ShapeMarker marker = new ShapeMarker(markerName);
      markers.add(marker);
    }
    return markers;
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
      final AffineTransform transform = AffineTransform.getTranslateInstance(-bounds.x, -bounds.y);
      transform.concatenate(AffineTransform.getScaleInstance(scale, scale));
      this.shape = new GeneralPath(shape).createTransformedShape(transform);
    }
  }

  public ShapeMarker(final String name) {
    this(SHAPES.get(name));
    this.name = name;
    if (this.getShape() == null) {
      throw new IllegalArgumentException("Unknown shape " + name);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof ShapeMarker) {
      final ShapeMarker marker = (ShapeMarker)object;
      return getName().equals(marker.getName());
    } else {
      return false;
    }
  }

  @Override
  public Icon getIcon(final MarkerStyle style) {
    final Shape shape = getShape();
    final AffineTransform shapeTransform = AffineTransform.getScaleInstance(16, 16);

    final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();
    final Shape newShape = new GeneralPath(shape).createTransformedShape(shapeTransform);
    if (style.setMarkerFillStyle(null, graphics)) {
      graphics.fill(newShape);
    }
    if (style.setMarkerLineStyle(null, graphics)) {
      graphics.draw(newShape);
    }
    graphics.dispose();
    return new ImageIcon(image);
  }

  public String getName() {
    if (Property.hasValue(this.name)) {
      return this.name;
    } else {
      return "unknown";
    }
  }

  public Shape getShape() {
    return this.shape;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics, final MarkerStyle style,
    final double modelX, final double modelY, double orientation) {

    final AffineTransform savedTransform = graphics.getTransform();
    try {
      final Measure<Length> markerWidth = style.getMarkerWidth();
      final double mapWidth = Viewport2D.toDisplayValue(viewport, markerWidth);
      final Measure<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = Viewport2D.toDisplayValue(viewport, markerHeight);
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }

      translateMarker(viewport, graphics, style, modelX, modelY, mapWidth, mapHeight, orientation);

      final AffineTransform shapeTransform = AffineTransform.getScaleInstance(mapWidth, mapHeight);
      final Shape newShape = new GeneralPath(this.getShape()).createTransformedShape(shapeTransform);
      if (style.setMarkerFillStyle(viewport, graphics)) {
        graphics.fill(newShape);
      }
      if (style.setMarkerLineStyle(viewport, graphics)) {
        graphics.draw(newShape);
      }
    } finally {
      graphics.setTransform(savedTransform);
    }
  }

  @Override
  public String toString() {
    return getName();
  }
}

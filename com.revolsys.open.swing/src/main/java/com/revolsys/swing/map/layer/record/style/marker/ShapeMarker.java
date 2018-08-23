package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.symbol.Symbol;
import com.revolsys.swing.map.symbol.SymbolLibrary;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRender;

public class ShapeMarker extends AbstractMarker {
  private static final Map<String, Shape> SHAPES = new TreeMap<>();

  static {
    final SymbolLibrary symbolLibrary = SymbolLibrary.newSymbolLibrary("shapes", "Shapes");
    for (final Method method : ShapeMarker.class.getDeclaredMethods()) {
      final String methodName = method.getName();
      if (Modifier.isStatic(method.getModifiers())) {
        if (method.getReturnType().equals(Shape.class)) {
          try {
            final Shape shape = (Shape)method.invoke(null, 1);
            SHAPES.put(methodName, shape);
            symbolLibrary.addSymbolShape(methodName);
          } catch (final Throwable e) {
            e.printStackTrace();
          }
        }
      }
      SHAPES.put("rectangle", square(1));
      SHAPES.put("ellipse", circle(1));
    }
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

  public static Shape crossLine(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(size / 2, size);
    path.lineTo(size / 2, 0);
    path.moveTo(size, size / 2);
    path.lineTo(0, size / 2);
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

  public static List<ShapeMarker> getMarkers() {
    final List<ShapeMarker> markers = new ArrayList<>();
    for (final String markerName : SHAPES.keySet()) {
      final ShapeMarker marker = new ShapeMarker(markerName);
      markers.add(marker);
    }
    return markers;
  }

  public static void init() {
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

  public static Shape xLine(final double size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo(size, size);
    path.moveTo(0, size);
    path.lineTo(size, 0);
    return path;
  }

  private Shape shape;

  private Symbol symbol;

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

  public ShapeMarker(final String markerType) {
    this(SHAPES.get(markerType));
    setMarkerType(markerType);
    if (this.getShape() == null) {
      throw new IllegalArgumentException("Unknown shape " + markerType);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof ShapeMarker) {
      final ShapeMarker marker = (ShapeMarker)object;
      return getMarkerType().equals(marker.getMarkerType());
    } else {
      return false;
    }
  }

  public Shape getShape() {
    return this.shape;
  }

  @Override
  public boolean isUseMarkerType() {
    return true;
  }

  @Override
  public Icon newIcon(final MarkerStyle style) {
    final Shape shape = getShape();
    final AffineTransform shapeTransform = AffineTransform.getScaleInstance(15, 15);

    try (
      final ImageViewport viewport = new ImageViewport(16, 16)) {
      final Graphics2DViewRender view = viewport.newViewRenderer();
      final Graphics2D graphics = view.getGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      final Shape newShape = new GeneralPath(shape).createTransformedShape(shapeTransform);
      if (style.setMarkerFillStyle(view, graphics)) {
        graphics.fill(newShape);
      }
      if (style.setMarkerLineStyle(view, graphics)) {
        graphics.draw(newShape);
      }
      graphics.dispose();
      return new ImageIcon(viewport.getImage());
    }
  }

  @Override
  protected void postSetMarkerType() {
    final String markerType = getMarkerType();
    this.symbol = SymbolLibrary.findSymbol(markerType);
  }

  @Override
  public void render(final Graphics2DViewRender view, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY, double orientation) {

    try (
      BaseCloseable closable = view.useViewCoordinates()) {
      final Quantity<Length> markerWidth = style.getMarkerWidth();
      final double mapWidth = view.toDisplayValue(markerWidth);
      final Quantity<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = view.toDisplayValue(markerHeight);
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }

      translateMarker(view, graphics, style, modelX, modelY, mapWidth, mapHeight, orientation);

      final AffineTransform shapeTransform = AffineTransform.getScaleInstance(mapWidth, mapHeight);
      final Shape newShape = new GeneralPath(getShape()).createTransformedShape(shapeTransform);
      if (style.setMarkerFillStyle(view, graphics)) {
        graphics.fill(newShape);
      }
      if (style.setMarkerLineStyle(view, graphics)) {
        graphics.draw(newShape);
      }
    }
  }

  @Override
  public String toString() {
    if (this.symbol == null) {
      return super.toString();
    } else {
      return this.symbol.getTitle();
    }
  }

}

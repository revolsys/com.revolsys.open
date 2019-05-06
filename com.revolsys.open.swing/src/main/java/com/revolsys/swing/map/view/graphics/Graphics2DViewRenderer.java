package com.revolsys.swing.map.view.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.logging.Logs;

import com.revolsys.awt.ResetAffineTransform;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.Record;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.shape.LineStringShape;
import com.revolsys.swing.map.layer.record.renderer.shape.PolygonShape;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.view.TextStyleViewRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Debug;
import com.revolsys.util.Property;

import tec.uom.se.quantity.Quantities;

public class Graphics2DViewRenderer extends ViewRenderer {

  public static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

  private Graphics2D graphics;

  private ResetAffineTransform useModelTransform;

  private ResetAffineTransform useViewTransform;

  private final LineStringShape lineStringShape = new LineStringShape();

  private final PolygonShape polygonShape = new PolygonShape();

  protected AffineTransform canvasOriginalTransform = IDENTITY_TRANSFORM;

  protected AffineTransform canvasModelTransform = IDENTITY_TRANSFORM;

  protected AffineTransform modelToScreenTransform;

  public Graphics2DViewRenderer(final Graphics2D graphics, final int width, final int height) {
    super(null);
    this.geometryFactory = GeometryFactory.DEFAULT_2D;
    this.viewWidthPixels = width;
    this.viewHeightPixels = height;
    setGraphics(graphics);
  }

  public Graphics2DViewRenderer(final Viewport2D viewport) {
    this(viewport, null);
  }

  public Graphics2DViewRenderer(final Viewport2D viewport, final Graphics2D graphics) {
    super(viewport);
    setGraphics(graphics);
  }

  @Override
  public void drawGeometry(final Geometry geometry, final GeometryStyle style) {
    if (this.boundingBox.bboxIntersects(geometry)) {
      if (geometry.isGeometryCollection()) {
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          drawGeometry(part, style);
        }
      } else {
        this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
        final GeometryFactory viewGeometryFactory = this.geometryFactory;
        final Geometry convertedGeometry = geometry.as2d(viewGeometryFactory);
        if (convertedGeometry instanceof Point) {
          final Point point = (Point)convertedGeometry;
          drawMarker(style, point, 0);
        } else if (convertedGeometry instanceof LineString) {
          final LineString line = (LineString)convertedGeometry;
          final LineStringShape shape = this.lineStringShape;
          shape.setGeometry(line);
          drawShape(shape, style);
          shape.clearGeometry();
        } else if (convertedGeometry instanceof Polygon) {
          try (
            BaseCloseable transformCloseable = useModelCoordinates()) {
            final Polygon polygon = (Polygon)convertedGeometry;
            final PolygonShape shape = this.polygonShape;
            shape.setGeometry(polygon);
            fillShape(shape, style);
            drawShape(shape, style);
            shape.clearGeometry();
          }
        }
      }
    }
  }

  @Override
  public void drawGeometryOutline(final GeometryStyle style, final Geometry geometry) {
    if (this.boundingBox.bboxIntersects(geometry)) {
      if (geometry.isGeometryCollection()) {
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          drawGeometryOutline(style, part);
        }
      } else {
        this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
        final GeometryFactory viewGeometryFactory = this.geometryFactory;
        final Geometry convertedGeometry = geometry.as2d(viewGeometryFactory);
        if (convertedGeometry instanceof Point) {
          final Point point = (Point)convertedGeometry;
          drawMarker(style, point, 0);
        } else if (convertedGeometry instanceof LineString) {
          final LineString line = (LineString)convertedGeometry;
          final LineStringShape shape = this.lineStringShape;
          shape.setGeometry(line);
          drawShape(shape, style);
          shape.clearGeometry();
        } else if (convertedGeometry instanceof Polygon) {
          try (
            BaseCloseable transformCloseable = useModelCoordinates()) {
            final Polygon polygon = (Polygon)convertedGeometry;
            final PolygonShape shape = this.polygonShape;
            shape.setGeometry(polygon);
            drawShape(shape, style);
            shape.clearGeometry();
          }
        }
      }
    }
  }

  @Override
  public void drawImage(final GeoreferencedImage image, final boolean useTransform) {
    drawImage(image, useTransform, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
  }

  @Override
  public void drawImage(final GeoreferencedImage image, final boolean useTransform,
    final double alpha, final Object interpolationMethod) {
    if (image != null) {
      final Composite composite = this.graphics.getComposite();
      try {
        AlphaComposite alphaComposite = AlphaComposite.SrcOver;
        if (alpha < 1) {
          alphaComposite = alphaComposite.derive((float)alpha);
        }
        this.graphics.setComposite(alphaComposite);
        drawImage(image, useTransform, interpolationMethod);
      } finally {
        this.graphics.setComposite(composite);
      }
    }
  }

  @Override
  public void drawImage(final GeoreferencedImage image, final boolean useTransform,
    final Object interpolationMethod) {
    if (image != null) {
      try (
        BaseCloseable transformCloseable = useViewCoordinates()) {
        final BoundingBox viewBoundingBox = getBoundingBox();
        final int viewWidth = (int)Math.ceil(getViewWidthPixels());
        final int viewHeight = (int)Math.ceil(getViewHeightPixels());
        image.drawImage(this.graphics, viewBoundingBox, viewWidth, viewHeight, useTransform,
          interpolationMethod);
      }
    }
  }

  @Override
  public void drawLines(final GeometryStyle style, final Iterable<LineString> lines) {
    final LineStringShape shape = this.lineStringShape;
    final Graphics2D graphics = this.graphics;
    final Color originalColor = graphics.getColor();
    final Stroke originalStroke = graphics.getStroke();
    try (
      BaseCloseable useModelCoordinates = useModelCoordinates()) {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      final Color color = style.getLineColor();
      graphics.setColor(color);

      final Quantity<Length> lineWidth = style.getLineWidth();
      final Unit<Length> unit = lineWidth.getUnit();
      final float width = (float)toModelValue(lineWidth);

      final float dashOffset = (float)toModelValue(
        Quantities.getQuantity(style.getLineDashOffset(), unit));

      final float[] dashArray;
      final List<Double> lineDashArray = style.getLineDashArray();
      final int dashArraySize = lineDashArray.size();
      if (dashArraySize == 0) {
        dashArray = null;
      } else {
        dashArray = new float[dashArraySize];
        for (int i = 0; i < dashArray.length; i++) {
          final Double dashDouble = lineDashArray.get(i);
          final float dashFloat = (float)toModelValue(Quantities.getQuantity(dashDouble, unit));
          dashArray[i] = dashFloat;
        }
      }

      final int lineCap = style.getLineCap().getAwtValue();
      final int lineJoin = style.getLineJoin().getAwtValue();
      final BasicStroke basicStroke = new BasicStroke(width, lineCap, lineJoin,
        style.getLineMiterlimit(), dashArray, dashOffset);
      graphics.setStroke(basicStroke);

      for (final LineString line : lines) {
        try {
          shape.setGeometry(line);
          this.graphics.draw(shape);
        } catch (final TopologyException e) {
        }
      }
    } finally {
      shape.clearGeometry();
      graphics.setColor(originalColor);
      graphics.setStroke(originalStroke);
    }
  }

  @Override
  public void drawMarker(final Geometry geometry) {
    if (geometry.isGeometryCollection()) {
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry part = geometry.getGeometry(i);
        drawMarker(part);
      }
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final LineStringShape shape = this.lineStringShape;
      shape.setGeometry(line);
      this.graphics.draw(shape);
      shape.clearGeometry();
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final PolygonShape shape = this.polygonShape;
      shape.setGeometry(polygon);
      this.graphics.draw(shape);
      shape.clearGeometry();
    }
  }

  /**
   * Point must be in the same geometry factory as the view.
   * @param style
   * @param point
   * @param viewport
   */
  @Override
  public void drawMarker(final MarkerStyle style, Point point, final double orientation) {
    point = getGeometry(point);
    if (Property.hasValue(point)) {
      final Paint paint = this.graphics.getPaint();
      try {
        final Marker marker = style.getMarker();
        final double x = point.getX();
        final double y = point.getY();
        marker.render(this, this.graphics, style, x, y, orientation);
      } catch (final Throwable e) {
        Logs.debug(MarkerStyleRenderer.class, "Unable to render marker: " + style, e);
      } finally {
        this.graphics.setPaint(paint);
      }
    }
  }

  private void drawShape(final Shape shape, final GeometryStyle style) {
    if (style.getLineOpacity() > 0) {
      final Graphics2D graphics = this.graphics;
      final Color color = graphics.getColor();
      final Stroke stroke = graphics.getStroke();
      try (
        BaseCloseable transformCloseable = useModelCoordinates()) {
        style.applyLineStyle(this, graphics);
        graphics.draw(shape);
      } finally {
        graphics.setColor(color);
        graphics.setStroke(stroke);
      }
    }
  }

  @Override
  public void drawText(final Record record, final Geometry geometry, final TextStyle style) {
    if (geometry != null) {
      final String label = style.getLabel(record);
      for (final Geometry part : geometry.geometries()) {
        if (Property.hasValue(label) && part != null || this == null) {
          final String textPlacementType = style.getTextPlacementType();
          final PointDoubleXYOrientation point = AbstractRecordLayerRenderer
            .getPointWithOrientation(this, part, textPlacementType);
          if (point != null) {
            double orientation;
            final String orientationType = style.getTextOrientationType();
            if ("none".equals(orientationType)) {
              orientation = 0;
            } else {
              orientation = point.getOrientation();
              if (orientation > 270) {
                orientation -= 360;
              }
            }
            orientation += style.getTextOrientation();

            final Graphics2D graphics = this.graphics;
            final Paint paint = graphics.getPaint();
            final Composite composite = graphics.getComposite();
            try (
              BaseCloseable transformClosable = useViewCoordinates()) {
              graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
              graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

              final double x = point.getX();
              final double y = point.getY();
              final double[] location = toViewCoordinates(x, y);

              style.setTextStyle(this, graphics);

              final Quantity<Length> textDx = style.getTextDx();
              double dx = toDisplayValue(textDx);

              final Quantity<Length> textDy = style.getTextDy();
              double dy = -toDisplayValue(textDy);

              final FontMetrics fontMetrics = graphics.getFontMetrics();

              double maxWidth = 0;
              final String[] lines = label.split("[\\r\\n]");
              for (final String line : lines) {
                final Rectangle2D bounds = fontMetrics.getStringBounds(line, graphics);
                final double width = bounds.getWidth();
                maxWidth = Math.max(width, maxWidth);
              }
              final int descent = fontMetrics.getDescent();
              final int ascent = fontMetrics.getAscent();
              final int leading = fontMetrics.getLeading();
              final double maxHeight = lines.length * (ascent + descent)
                + (lines.length - 1) * leading;
              final String verticalAlignment = style.getTextVerticalAlignment();
              if ("top".equals(verticalAlignment)) {
              } else if ("middle".equals(verticalAlignment)) {
                dy -= maxHeight / 2;
              } else {
                dy -= maxHeight;
              }

              String horizontalAlignment = style.getTextHorizontalAlignment();
              double screenX = location[0];
              double screenY = location[1];
              final String textPlacement = textPlacementType;
              if ("auto".equals(textPlacement) && this != null) {
                if (screenX < 0) {
                  screenX = 1;
                  dx = 0;
                  horizontalAlignment = "left";
                }
                final double viewWidth = this.getViewWidthPixels();
                if (screenX + maxWidth > viewWidth) {
                  screenX = (int)(viewWidth - maxWidth - 1);
                  dx = 0;
                  horizontalAlignment = "left";
                }
                if (screenY < maxHeight) {
                  screenY = 1;
                  dy = 0;
                }
                final double viewHeight = this.getViewHeightPixels();
                if (screenY > viewHeight) {
                  screenY = viewHeight - 1 - maxHeight;
                  dy = 0;
                }
              }
              graphics.translate(screenX, screenY);
              if (orientation != 0) {
                graphics.rotate(-Math.toRadians(orientation), 0, 0);
              }
              graphics.translate(dx, dy);
              for (final String line : lines) {
                graphics.translate(0, ascent);
                final AffineTransform lineTransform = graphics.getTransform();
                final Rectangle2D bounds = fontMetrics.getStringBounds(line, graphics);
                final double width = bounds.getWidth();
                final double height = bounds.getHeight();

                if ("right".equals(horizontalAlignment)) {
                  graphics.translate(-width, 0);
                } else if ("center".equals(horizontalAlignment)
                  || "auto".equals(horizontalAlignment)) {
                  graphics.translate(-width / 2, 0);
                }
                graphics.translate(dx, 0);

                graphics.scale(1, 1);
                if (Math.abs(orientation) > 90) {
                  graphics.rotate(Math.PI, maxWidth / 2, -height / 4);
                }

                final int textBoxOpacity = style.getTextBoxOpacity();
                final Color textBoxColor = style.getTextBoxColor();
                if (textBoxOpacity > 0 && textBoxColor != null) {
                  graphics.setPaint(textBoxColor);
                  final double cornerSize = Math.max(height / 2, 5);
                  final RoundRectangle2D.Double box = new RoundRectangle2D.Double(bounds.getX() - 3,
                    bounds.getY() - 1, width + 6, height + 2, cornerSize, cornerSize);
                  graphics.fill(box);
                }

                final double radius = style.getTextHaloRadius();
                final Unit<Length> unit = style.getTextSizeUnit();
                final double textHaloRadius = this
                  .toDisplayValue(Quantities.getQuantity(radius, unit));
                if (textHaloRadius > 0) {
                  final Stroke savedStroke = graphics.getStroke();
                  final Stroke outlineStroke = new BasicStroke((float)(textHaloRadius + 1),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
                  graphics.setColor(style.getTextHaloFill());
                  graphics.setStroke(outlineStroke);
                  final Font font = graphics.getFont();
                  final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
                  final TextLayout textLayout = new TextLayout(line, font, fontRenderContext);
                  final Shape outlineShape = textLayout.getOutline(IDENTITY_TRANSFORM);
                  graphics.draw(outlineShape);
                  graphics.setStroke(savedStroke);
                }

                graphics.setColor(style.getTextFill());
                if (textBoxOpacity > 0 && textBoxOpacity < 255) {
                  graphics.setComposite(AlphaComposite.SrcOut);
                  graphics.drawString(line, (float)0, (float)0);
                  graphics.setComposite(AlphaComposite.DstOver);
                  graphics.drawString(line, (float)0, (float)0);

                } else {
                  graphics.setComposite(AlphaComposite.SrcOver);
                  graphics.drawString(line, (float)0, (float)0);
                }

                graphics.setTransform(lineTransform);
                graphics.translate(0, leading + descent);
              }
            } finally {
              graphics.setPaint(paint);
              graphics.setComposite(composite);
            }
          }
        }
      }
    }
  }

  @Override
  public void fillMarker(final Geometry geometry) {
    if (geometry.isGeometryCollection()) {
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry part = geometry.getGeometry(i);
        fillMarker(part);
      }
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final PolygonShape shape = this.polygonShape;
      shape.setGeometry(polygon);
      this.graphics.fill(shape);
      shape.clearGeometry();
    }
  }

  @Override
  public void fillPolygons(final GeometryStyle style, final Iterable<Polygon> polygons) {
    final Graphics2D graphics = this.graphics;
    final PolygonShape shape = this.polygonShape;
    final Paint originalPaint = graphics.getPaint();
    try (
      BaseCloseable useModelCoordinates = useModelCoordinates()) {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      graphics.setPaint(style.getPolygonFill());
      // final Graphic fillPattern = fill.getPattern();
      // if (fillPattern != null) {
      // TODO fillPattern
      // double width = fillPattern.getWidth();
      // double height = fillPattern.getHeight();
      // Rectangle2D.Double patternRect;
      // // TODO units
      // // if (isUseModelUnits()) {
      // // patternRect = new Rectangle2D.Double(0, 0, width
      // // * viewport.getModelUnitsPerViewUnit(), height
      // // * viewport.getModelUnitsPerViewUnit());
      // // } else {
      // patternRect = new Rectangle2D.Double(0, 0, width, height);
      // // }
      // graphics.setPaint(new TexturePaint(fillPattern, patternRect));

      // }
      for (final Polygon polygon : polygons) {
        try {
          shape.setGeometry(polygon);
          graphics.fill(shape);
        } catch (final TopologyException e) {
          Debug.noOp();
        }
      }
    } finally {
      shape.clearGeometry();
      graphics.setPaint(originalPaint);
    }
  }

  private void fillShape(final Shape shape, final GeometryStyle style) {
    final Graphics2D graphics = this.graphics;
    if (style.getPolygonFillOpacity() > 0) {
      final Paint paint = graphics.getPaint();
      try {
        graphics.setPaint(style.getPolygonFill());
        // final Graphic fillPattern = fill.getPattern();
        // if (fillPattern != null) {
        // TODO fillPattern
        // double width = fillPattern.getWidth();
        // double height = fillPattern.getHeight();
        // Rectangle2D.Double patternRect;
        // // TODO units
        // // if (isUseModelUnits()) {
        // // patternRect = new Rectangle2D.Double(0, 0, width
        // // * viewport.getModelUnitsPerViewUnit(), height
        // // * viewport.getModelUnitsPerViewUnit());
        // // } else {
        // patternRect = new Rectangle2D.Double(0, 0, width, height);
        // // }
        // graphics.setPaint(new TexturePaint(fillPattern, patternRect));

        // }
        graphics.fill(shape);
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  public Graphics2D getGraphics() {
    return this.graphics;
  }

  @Override
  public TextStyleViewRenderer newTextStyleViewRenderer(final TextStyle textStyle) {
    return new Graphics2DTextStyleRenderer(this, textStyle);
  }

  @Override
  public void renderEllipse(final MarkerStyle style, final double modelX, final double modelY,
    double orientation) {
    try (
      BaseCloseable closable = useViewCoordinates()) {
      final Quantity<Length> markerWidth = style.getMarkerWidth();
      final double mapWidth = toDisplayValue(markerWidth);
      final Quantity<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = toDisplayValue(markerHeight);
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }

      translateMarker(style, modelX, modelY, mapWidth, mapHeight, orientation);

      final Ellipse2D ellipse = new Ellipse2D.Double(0, 0, mapWidth, mapHeight);
      if (style.setMarkerFillStyle(this, this.graphics)) {
        this.graphics.fill(ellipse);
      }
      if (style.setMarkerLineStyle(this, this.graphics)) {
        this.graphics.draw(ellipse);
      }
    }
  }

  public void renderRectangle(final MarkerStyle style, final double modelX, final double modelY,
    double orientation) {
    try (
      BaseCloseable closable = useViewCoordinates()) {
      final Quantity<Length> markerWidth = style.getMarkerWidth();
      final double mapWidth = toDisplayValue(markerWidth);
      final Quantity<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = toDisplayValue(markerHeight);
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }

      translateMarker(style, modelX, modelY, mapWidth, mapHeight, orientation);

      final Rectangle2D rectangle = new Rectangle2D.Double(0, 0, mapWidth, mapHeight);
      if (style.setMarkerFillStyle(this, this.graphics)) {
        this.graphics.fill(rectangle);
      }
      if (style.setMarkerLineStyle(this, this.graphics)) {
        this.graphics.draw(rectangle);
      }
    }
  }

  public void setGraphics(final Graphics2D graphics) {
    this.graphics = graphics;
    updateFields();
  }

  @Override
  public double[] toViewCoordinates(final double x, final double y) {
    final double[] ordinates = new double[] {
      x, y
    };
    final AffineTransform transform = this.modelToScreenTransform;
    if (transform == null) {
      return ordinates;
    } else {
      transform.transform(ordinates, 0, ordinates, 0, 1);
      return ordinates;
    }
  }

  @Override
  public void toViewCoordinates(final double[] coordinates) {
    if (!this.modelToScreenTransform.isIdentity()) {
      this.modelToScreenTransform.transform(coordinates, 0, coordinates, 0, 1);
    }
  }

  public void translateMarker(final MarkerStyle style, final double x, final double y,
    final double width, final double height, double orientation) {
    translateModelToViewCoordinates(x, y);
    final double markerOrientation = style.getMarkerOrientation();
    orientation = -orientation + markerOrientation;
    if (orientation != 0) {
      this.graphics.rotate(Math.toRadians(orientation));
    }

    final Quantity<Length> deltaX = style.getMarkerDx();
    final Quantity<Length> deltaY = style.getMarkerDy();
    double dx = toDisplayValue(deltaX);
    double dy = toDisplayValue(deltaY);
    final String verticalAlignment = style.getMarkerVerticalAlignment();
    if ("bottom".equals(verticalAlignment)) {
      dy -= height;
    } else if ("auto".equals(verticalAlignment) || "middle".equals(verticalAlignment)) {
      dy -= height / 2;
    }
    final String horizontalAlignment = style.getMarkerHorizontalAlignment();
    if ("right".equals(horizontalAlignment)) {
      dx -= width;
    } else if ("auto".equals(horizontalAlignment) || "center".equals(horizontalAlignment)) {
      dx -= width / 2;
    }
    this.graphics.translate(dx, dy);
  }

  public void translateModelToViewCoordinates(final double modelX, final double modelY) {
    final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
    final double viewX = viewCoordinates[0];
    final double viewY = viewCoordinates[1];
    this.graphics.translate(viewX, viewY);
  }

  @Override
  protected void updateFields() {
    if (this.graphics == null) {
      this.canvasOriginalTransform = IDENTITY_TRANSFORM;
    } else {
      this.canvasOriginalTransform = this.graphics.getTransform();
      if (this.canvasOriginalTransform == null) {
        this.canvasOriginalTransform = IDENTITY_TRANSFORM;
      }
    }
    super.updateFields();

    if (this.viewport == null) {
      this.modelToScreenTransform = IDENTITY_TRANSFORM;
    } else {
      this.modelToScreenTransform = this.viewport.getModelToScreenTransform();
    }
    if (this.modelToScreenTransform == null) {
      this.canvasModelTransform = IDENTITY_TRANSFORM;
    } else {
      final AffineTransform transform = (AffineTransform)this.canvasOriginalTransform.clone();
      transform.concatenate(this.modelToScreenTransform);
      this.canvasModelTransform = transform;
    }
    this.useModelTransform = new ResetAffineTransform(this.graphics, this.canvasOriginalTransform,
      this.canvasModelTransform);
    this.useViewTransform = new ResetAffineTransform(this.graphics, this.canvasOriginalTransform,
      this.canvasOriginalTransform);
  }

  public BaseCloseable useModelCoordinates() {
    return this.useModelTransform.reset();
  }

  public BaseCloseable useViewCoordinates() {
    return this.useViewTransform.reset();
  }

}

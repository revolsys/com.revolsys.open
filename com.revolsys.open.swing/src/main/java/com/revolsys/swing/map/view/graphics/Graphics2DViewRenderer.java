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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

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
          drawMarker(point, style, 0);
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
  public void drawGeometryOutline(final Geometry geometry, final GeometryStyle style) {
    if (this.boundingBox.bboxIntersects(geometry)) {
      if (geometry.isGeometryCollection()) {
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          drawGeometryOutline(part, style);
        }
      } else {
        this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
        final GeometryFactory viewGeometryFactory = this.geometryFactory;
        final Geometry convertedGeometry = geometry.as2d(viewGeometryFactory);
        if (convertedGeometry instanceof Point) {
          final Point point = (Point)convertedGeometry;
          drawMarker(point, style, 0);
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
        final int viewWidth = getViewWidthPixels();
        final int viewHeight = getViewHeightPixels();
        image.drawImage(this.graphics, viewBoundingBox, viewWidth, viewHeight, useTransform,
          interpolationMethod);
      }
    }
  }

  /**
   * Point must be in the same geometry factory as the view.
   *
   * @param viewport
   * @param point
   * @param style
   */
  @Override
  public void drawMarker(Point point, final MarkerStyle style, final double orientation) {
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

            final Paint paint = this.graphics.getPaint();
            final Composite composite = this.graphics.getComposite();
            try {
              this.graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
              this.graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

              final double x = point.getX();
              final double y = point.getY();
              final double[] location = this.toViewCoordinates(x, y);

              final AffineTransform savedTransform = this.graphics.getTransform();

              style.setTextStyle(this, this.graphics);

              final Quantity<Length> textDx = style.getTextDx();
              double dx = this.toDisplayValue(textDx);

              final Quantity<Length> textDy = style.getTextDy();
              double dy = -this.toDisplayValue(textDy);

              final FontMetrics fontMetrics = this.graphics.getFontMetrics();

              double maxWidth = 0;
              final String[] lines = label.split("[\\r\\n]");
              for (final String line : lines) {
                final Rectangle2D bounds = fontMetrics.getStringBounds(line, this.graphics);
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
                final int viewWidth = this.getViewWidthPixels();
                if (screenX + maxWidth > viewWidth) {
                  screenX = (int)(viewWidth - maxWidth - 1);
                  dx = 0;
                  horizontalAlignment = "left";
                }
                if (screenY < maxHeight) {
                  screenY = 1;
                  dy = 0;
                }
                final int viewHeight = this.getViewHeightPixels();
                if (screenY > viewHeight) {
                  screenY = viewHeight - 1 - maxHeight;
                  dy = 0;
                }
              }
              this.graphics.translate(screenX, screenY);
              if (orientation != 0) {
                this.graphics.rotate(-Math.toRadians(orientation), 0, 0);
              }
              this.graphics.translate(dx, dy);
              for (final String line : lines) {
                this.graphics.translate(0, ascent);
                final AffineTransform lineTransform = this.graphics.getTransform();
                final Rectangle2D bounds = fontMetrics.getStringBounds(line, this.graphics);
                final double width = bounds.getWidth();
                final double height = bounds.getHeight();

                if ("right".equals(horizontalAlignment)) {
                  this.graphics.translate(-width, 0);
                } else if ("center".equals(horizontalAlignment)
                  || "auto".equals(horizontalAlignment)) {
                  this.graphics.translate(-width / 2, 0);
                }
                this.graphics.translate(dx, 0);

                this.graphics.scale(1, 1);
                if (Math.abs(orientation) > 90) {
                  this.graphics.rotate(Math.PI, maxWidth / 2, -height / 4);
                }

                final int textBoxOpacity = style.getTextBoxOpacity();
                final Color textBoxColor = style.getTextBoxColor();
                if (textBoxOpacity > 0 && textBoxColor != null) {
                  this.graphics.setPaint(textBoxColor);
                  final double cornerSize = Math.max(height / 2, 5);
                  final RoundRectangle2D.Double box = new RoundRectangle2D.Double(bounds.getX() - 3,
                    bounds.getY() - 1, width + 6, height + 2, cornerSize, cornerSize);
                  this.graphics.fill(box);
                }

                final double radius = style.getTextHaloRadius();
                final Unit<Length> unit = style.getTextSizeUnit();
                final double textHaloRadius = this
                  .toDisplayValue(Quantities.getQuantity(radius, unit));
                if (textHaloRadius > 0) {
                  final Stroke savedStroke = this.graphics.getStroke();
                  final Stroke outlineStroke = new BasicStroke((float)(textHaloRadius + 1),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
                  this.graphics.setColor(style.getTextHaloFill());
                  this.graphics.setStroke(outlineStroke);
                  final Font font = this.graphics.getFont();
                  final FontRenderContext fontRenderContext = this.graphics.getFontRenderContext();
                  final TextLayout textLayout = new TextLayout(line, font, fontRenderContext);
                  final Shape outlineShape = textLayout.getOutline(IDENTITY_TRANSFORM);
                  this.graphics.draw(outlineShape);
                  this.graphics.setStroke(savedStroke);
                }

                this.graphics.setColor(style.getTextFill());
                if (textBoxOpacity > 0 && textBoxOpacity < 255) {
                  this.graphics.setComposite(AlphaComposite.SrcOut);
                  this.graphics.drawString(line, (float)0, (float)0);
                  this.graphics.setComposite(AlphaComposite.DstOver);
                  this.graphics.drawString(line, (float)0, (float)0);

                } else {
                  this.graphics.setComposite(AlphaComposite.SrcOver);
                  this.graphics.drawString(line, (float)0, (float)0);
                }

                this.graphics.setTransform(lineTransform);
                this.graphics.translate(0, leading + descent);
              }
              this.graphics.setTransform(savedTransform);

            } finally {
              this.graphics.setPaint(paint);
              this.graphics.setComposite(composite);
            }
          }
        }
      }
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

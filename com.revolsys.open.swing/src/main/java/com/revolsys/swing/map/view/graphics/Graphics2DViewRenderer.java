package com.revolsys.swing.map.view.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.function.BiFunctionDouble;
import org.w3c.dom.Document;

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
import com.revolsys.swing.Fonts;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.ViewportCacheBoundingBox;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.shape.LineStringShape;
import com.revolsys.swing.map.layer.record.renderer.shape.PolygonShape;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.layer.record.style.marker.AbstractMarkerRenderer;
import com.revolsys.swing.map.layer.record.style.marker.GeometryMarker;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.layer.record.style.marker.SvgBufferedImageTranscoder;
import com.revolsys.swing.map.layer.record.style.marker.SvgMarker;
import com.revolsys.swing.map.layer.record.style.marker.TextMarker;
import com.revolsys.swing.map.view.TextStyleViewRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;

import tech.units.indriya.quantity.Quantities;

public class Graphics2DViewRenderer extends ViewRenderer {
  private abstract class Graphics2DMarkerRenderer extends AbstractMarkerRenderer {
    public Graphics2DMarkerRenderer(final MarkerStyle style) {
      super(Graphics2DViewRenderer.this, style);
    }

    @Override
    protected void translateDo(final double x, final double y, final double orientation,
      final double dx, final double dy) {
      translateModelToViewCoordinates(x, y);
      final Graphics2D graphics = Graphics2DViewRenderer.this.graphics;
      if (orientation != 0) {
        graphics.rotate(Math.toRadians(orientation));
      }
      graphics.translate(dx, dy);
    }
  }

  private class MarkerRendererGeometry extends Graphics2DMarkerRenderer {
    private final Geometry geometry;

    public MarkerRendererGeometry(final GeometryMarker marker, final MarkerStyle style) {
      super(style);
      this.geometry = marker.newMarker(this.mapWidth, this.mapHeight);
    }

    @Override
    public void renderMarkerDo() {
      final Graphics2D graphics = Graphics2DViewRenderer.this.graphics;
      final MarkerStyle style = this.style;
      final Geometry geometry = this.geometry;
      graphics.setPaint(style.getMarkerFill());
      fillMarkerGeometry(geometry);
      graphics.setColor(style.getMarkerLineColor());
      drawMarkerGeometry(geometry);
    }
  }

  private class MarkerRendererImage extends Graphics2DMarkerRenderer {
    private final Image image;

    private final AffineTransform imageTransform;

    public MarkerRendererImage(final ImageMarker imageMarker, final MarkerStyle style) {
      super(style);
      this.image = imageMarker.getImage();
      this.imageTransform = AffineTransform.getScaleInstance(
        this.mapWidth / this.image.getWidth(null), this.mapHeight / this.image.getHeight(null));
    }

    @Override
    protected void renderMarkerDo() {
      final Image image = this.image;
      if (image != null) {
        Graphics2DViewRenderer.this.graphics.drawImage(image, this.imageTransform, null);
      }
    }
  }

  private class MarkerRendererShape extends Graphics2DMarkerRenderer {

    private final Color fillColor;

    private final Color lineColor;

    private final Shape shape;

    public MarkerRendererShape(final MarkerStyle style,
      final BiFunctionDouble<? extends Shape> shapeConstructor) {
      super(style);
      this.shape = shapeConstructor.accept(this.mapWidth, this.mapHeight);
      this.fillColor = this.style.getMarkerFill();
      this.lineColor = this.style.getMarkerLineColor();
    }

    @Override
    protected void renderMarkerDo() {
      final Graphics2D graphics = Graphics2DViewRenderer.this.graphics;
      graphics.setPaint(this.fillColor);
      graphics.fill(this.shape);
      graphics.setColor(this.lineColor);
      graphics.draw(this.shape);
    }
  }

  private class MarkerRendererSvg extends Graphics2DMarkerRenderer {

    private final BufferedImage image;

    public MarkerRendererSvg(final SvgMarker marker, final MarkerStyle style) {
      super(style);
      final Document document = marker.getDocument();
      if (document == null) {
        this.image = null;
      } else {
        final String uri = marker.getUri();
        this.image = SvgBufferedImageTranscoder.newImage(document, uri,
          (int)Math.round(this.mapWidth), (int)Math.round(this.mapHeight));
      }
    }

    @Override
    protected void renderMarkerDo() {
      if (this.image != null) {
        Graphics2DViewRenderer.this.graphics.drawImage(this.image, 0, 0, null);
      }
    }
  }

  private class MarkerRendererText extends Graphics2DMarkerRenderer {

    private final Font font;

    private final String text;

    public MarkerRendererText(final TextMarker textMarker, final MarkerStyle style) {
      super(style);
      final int fontSize = (int)this.mapHeight;
      this.font = Fonts.newFont(textMarker.getTextFaceName(), 0, fontSize);
      this.text = textMarker.getText();
    }

    @Override
    public void renderMarker(final double modelX, final double modelY, double orientation) {
      final MarkerStyle style = this.style;
      // TODO
      final Graphics2D graphics = getGraphics();
      try (
        BaseCloseable transformCloseable = useViewCoordinates()) {
        final String orientationType = style.getMarkerOrientationType();
        if ("none".equals(orientationType)) {
          orientation = 0;
        }

        final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
        final GlyphVector glyphVector = this.font.createGlyphVector(fontRenderContext, this.text);
        final Shape shape = glyphVector.getOutline();
        final GeneralPath newShape = new GeneralPath(shape);
        final Rectangle2D bounds = newShape.getBounds2D();
        final double shapeWidth = bounds.getWidth();
        final double shapeHeight = bounds.getHeight();

        translateModelToViewCoordinates(modelX, modelY);
        final double markerOrientation = style.getMarkerOrientation();
        orientation = -orientation + markerOrientation;
        if (orientation != 0) {
          graphics.rotate(Math.toRadians(orientation));
        }

        final Quantity<Length> deltaX = style.getMarkerDx();
        final Quantity<Length> deltaY = style.getMarkerDy();
        double dx = toDisplayValue(deltaX);
        double dy = toDisplayValue(deltaY);
        dy -= bounds.getY();
        final String verticalAlignment = style.getMarkerVerticalAlignment();
        if ("bottom".equals(verticalAlignment)) {
          dy -= shapeHeight;
        } else if ("auto".equals(verticalAlignment) || "middle".equals(verticalAlignment)) {
          dy -= shapeHeight / 2.0;
        }
        final String horizontalAlignment = style.getMarkerHorizontalAlignment();
        if ("right".equals(horizontalAlignment)) {
          dx -= shapeWidth;
        } else if ("auto".equals(horizontalAlignment) || "center".equals(horizontalAlignment)) {
          dx -= shapeWidth / 2;
        }
        graphics.translate(dx, dy);

        if (style.setMarkerFillStyle(Graphics2DViewRenderer.this, graphics)) {

          graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
          graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
          graphics.setFont(this.font);
          graphics.drawString(this.text, 0, 0);
        }
      }
    }
  }

  private class MarkerStyleCloseable implements BaseCloseable {
    private Color color;

    private Paint paint;

    private Stroke stroke;

    @Override
    public void close() {
      Graphics2DViewRenderer.this.graphics.setPaint(this.paint);
      Graphics2DViewRenderer.this.graphics.setColor(this.color);
      Graphics2DViewRenderer.this.graphics.setStroke(this.stroke);
    }

    public BaseCloseable reset(final MarkerStyle style) {
      final Graphics2D graphics = Graphics2DViewRenderer.this.graphics;
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

      this.paint = graphics.getPaint();
      this.stroke = graphics.getStroke();
      this.color = graphics.getColor();
      final Color markerFill = style.getMarkerFill();
      graphics.setPaint(markerFill);

      final Color markerLineColor = style.getMarkerLineColor();
      graphics.setColor(markerLineColor);
      final Quantity<Length> measure = style.getMarkerLineWidth();
      final float width = (float)toDisplayValue(measure);
      final BasicStroke basicStroke = new BasicStroke(width);
      graphics.setStroke(basicStroke);
      return this;
    }
  }

  public static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

  protected AffineTransform canvasModelTransform = IDENTITY_TRANSFORM;;

  protected AffineTransform canvasOriginalTransform = IDENTITY_TRANSFORM;

  private Graphics2D graphics;

  private final LineStringShape lineStringShape = new LineStringShape();

  private final MarkerStyleCloseable markerStyleCloseable = new MarkerStyleCloseable();

  protected AffineTransform modelToScreenTransform;

  private final PolygonShape polygonShape = new PolygonShape();

  private ResetAffineTransform useModelTransform;

  private ResetAffineTransform useViewTransform;

  private final double[] coordinates = new double[2];

  public Graphics2DViewRenderer(final Graphics2D graphics, final int width, final int height) {
    super(width, height);
    setGraphics(null, graphics);
  }

  public Graphics2DViewRenderer(final Viewport2D viewport) {
    this(viewport, null);
  }

  public Graphics2DViewRenderer(final Viewport2D viewport, final Graphics2D graphics) {
    super(viewport);
    setGraphics(viewport, graphics);
  }

  @Override
  public BaseCloseable applyMarkerStyle(final MarkerStyle style) {
    return this.markerStyleCloseable.reset(style);
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
          renderMarker(style, point);
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
          renderMarker(style, point);
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
        final Object oldInterpolationMethod = this.graphics
          .getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        if (interpolationMethod != null) {
          this.graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationMethod);
        }
        image.drawImage(this, (renderedImage, imageBoundingBox, geoTransform) -> {
          if (renderedImage != null) {
            final int imageWidth = renderedImage.getWidth();
            final int imageHeight = renderedImage.getHeight();
            final GeometryFactory viewGeometryFactory = viewBoundingBox.getGeometryFactory();
            imageBoundingBox = imageBoundingBox.bboxToCs(viewGeometryFactory);
            final double scaleFactor = viewWidth / viewBoundingBox.getWidth();

            final double imageMinX = imageBoundingBox.getMinX();
            final double viewMinX = viewBoundingBox.getMinX();
            final double screenX = (imageMinX - viewMinX) * scaleFactor;

            final double imageMaxY = imageBoundingBox.getMaxY();
            final double viewMaxY = viewBoundingBox.getMaxY();
            final double screenY = -(imageMaxY - viewMaxY) * scaleFactor;

            final double imageModelWidth = imageBoundingBox.getWidth();
            final int imageScreenWidth = (int)Math.ceil(imageModelWidth * scaleFactor);

            final double imageModelHeight = imageBoundingBox.getHeight();
            final int imageScreenHeight = (int)Math.ceil(imageModelHeight * scaleFactor);

            if (imageScreenWidth > 0 && imageScreenHeight > 0) {
              final double scaleX = (double)imageScreenWidth / imageWidth;
              final double scaleY = (double)imageScreenHeight / imageHeight;
              final AffineTransform imageTransform = new AffineTransform(scaleX, 0, 0, scaleY,
                screenX, screenY);
              if (useTransform) {
                imageTransform.concatenate(geoTransform);
              }

              this.graphics.drawRenderedImage(renderedImage, imageTransform);
            }
          }
        }, viewBoundingBox, viewWidth, viewHeight, useTransform);
        if (oldInterpolationMethod != null) {
          this.graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolationMethod);
        }

      }
    }
  }

  @Override
  public void drawLines(final GeometryStyle style, final Collection<LineString> lines) {
    if (!lines.isEmpty()) {
      final LineStringShape shape = this.lineStringShape;
      final Graphics2D graphics = this.graphics;
      final Color originalColor = graphics.getColor();
      final Stroke originalStroke = graphics.getStroke();
      try (
        BaseCloseable useModelCoordinates = useModelCoordinates()) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

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
  }

  private void drawMarkerGeometry(final Geometry geometry) {
    if (geometry.isGeometryCollection()) {
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry part = geometry.getGeometry(i);
        drawMarkerGeometry(part);
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
              for (int i = 0; i < lines.length; i++) {
                final String line = lines[i];
                graphics.translate(0, ascent);
                final AffineTransform lineTransform;
                if (i == lines.length - 1) {
                  lineTransform = null;
                } else {
                  lineTransform = graphics.getTransform();
                }
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

                if (lineTransform != null) {
                  graphics.setTransform(lineTransform);
                  graphics.translate(0, leading + descent);
                }
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

  private void fillMarkerGeometry(final Geometry geometry) {
    if (geometry.isGeometryCollection()) {
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry part = geometry.getGeometry(i);
        fillMarkerGeometry(part);
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
  public void fillPolygons(final GeometryStyle style, final Collection<Polygon> polygons) {
    if (!polygons.isEmpty()) {
      final Graphics2D graphics = this.graphics;
      final PolygonShape shape = this.polygonShape;
      final Paint originalPaint = graphics.getPaint();
      try (
        BaseCloseable useModelCoordinates = useModelCoordinates()) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

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
          }
        }
      } finally {
        shape.clearGeometry();
        graphics.setPaint(originalPaint);
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

  @Override
  public <V> V getCachedItemBackground(final String taskName, final Layer layer, final Object key,
    final Supplier<V> constructor, final Consumer<Throwable> errorHandler) {
    if (isBackgroundDrawingEnabled()) {
      return this.cacheBoundingBox.getCachedItemFuture(taskName, layer, key, constructor,
        errorHandler);
    } else {
      return super.getCachedItemBackground(taskName, layer, key, constructor, errorHandler);
    }
  }

  public Graphics2D getGraphics() {
    return this.graphics;
  }

  @Override
  public MarkerRenderer newMarkerRendererEllipse(final MarkerStyle style) {
    return new MarkerRendererShape(style,
      (width, height) -> new Ellipse2D.Double(0, 0, width, height));
  }

  @Override
  public MarkerRenderer newMarkerRendererGeometry(final GeometryMarker geometryMarker,
    final MarkerStyle style) {
    return new MarkerRendererGeometry(geometryMarker, style);
  }

  @Override
  public MarkerRenderer newMarkerRendererImage(final ImageMarker imageMarker,
    final MarkerStyle style) {
    return new MarkerRendererImage(imageMarker, style);
  }

  @Override
  public MarkerRenderer newMarkerRendererRectangle(final MarkerStyle style) {
    return new MarkerRendererShape(style,
      (width, height) -> new Rectangle2D.Double(0, 0, width, height));
  }

  @Override
  public MarkerRenderer newMarkerRendererSvg(final SvgMarker svgMarker, final MarkerStyle style) {
    return new MarkerRendererSvg(svgMarker, style);
  }

  @Override
  public MarkerRenderer newMarkerRendererText(final TextMarker textMarker,
    final MarkerStyle style) {
    return new MarkerRendererText(textMarker, style);
  }

  @Override
  public TextStyleViewRenderer newTextStyleViewRenderer(final TextStyle textStyle) {
    return new Graphics2DTextStyleRenderer(this, textStyle);
  }

  @Override
  protected void setCacheBoundingBox(final ViewportCacheBoundingBox cacheBoundingBox) {
    if (this.graphics == null) {
      this.canvasOriginalTransform = IDENTITY_TRANSFORM;
    } else {
      this.canvasOriginalTransform = this.graphics.getTransform();
      if (this.canvasOriginalTransform == null) {
        this.canvasOriginalTransform = IDENTITY_TRANSFORM;
      }
    }
    super.setCacheBoundingBox(cacheBoundingBox);

    if (hasViewport()) {
      final double mapWidth = this.boundingBox.getWidth();
      final double pixelsPerXUnit = this.viewWidthPixels / mapWidth;

      final double mapHeight = this.boundingBox.getHeight();
      final double pixelsPerYUnit = -this.viewHeightPixels / mapHeight;

      final double originX = this.boundingBox.getMinX();
      final double originY = this.boundingBox.getMaxY();
      final AffineTransform modelToScreenTransform = AffineTransform
        .getScaleInstance(pixelsPerXUnit, pixelsPerYUnit);
      modelToScreenTransform.translate(-originX, -originY);
      this.modelToScreenTransform = modelToScreenTransform;
    } else {
      this.modelToScreenTransform = IDENTITY_TRANSFORM;
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

  public void setGraphics(final Viewport2D viewport, final Graphics2D graphics) {
    this.graphics = graphics;
    if (viewport == null) {
      setCacheBoundingBox(new ViewportCacheBoundingBox(100, 100));
    } else {
      setCacheBoundingBox(viewport.getCacheBoundingBox());
    }
  }

  private double[] toViewCoordinates(final double x, final double y) {
    final double[] coordinates = this.coordinates;
    coordinates[0] = x;
    coordinates[1] = y;
    return toViewCoordinates(coordinates);
  }

  public double[] toViewCoordinates(final double[] coordinates) {
    final AffineTransform transform = this.modelToScreenTransform;
    if (transform == null) {
      return coordinates;
    } else {
      transform.transform(coordinates, 0, coordinates, 0, 1);
      return coordinates;
    }
  }

  public void translateModelToViewCoordinates(final double modelX, final double modelY) {
    final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
    final double viewX = viewCoordinates[0];
    final double viewY = viewCoordinates[1];
    this.graphics.translate(viewX, viewY);
  }

  public BaseCloseable useModelCoordinates() {
    return this.useModelTransform.reset();
  }

  @Override
  public BaseCloseable useViewCoordinates() {
    return this.useViewTransform.reset();
  }

}

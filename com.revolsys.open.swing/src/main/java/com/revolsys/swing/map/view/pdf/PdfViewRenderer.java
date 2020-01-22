package com.revolsys.swing.map.view.pdf;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.optionalcontent.PDOptionalContentGroup;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.jeometry.common.exception.Exceptions;
import org.w3c.dom.Document;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
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

public class PdfViewRenderer extends ViewRenderer {

  private abstract class PdfMarkerRenderer extends AbstractMarkerRenderer {
    protected final Matrix matrix = new Matrix();

    private final double minX;

    private final double minY;

    private final double modelUnitsPerViewUnit;

    public PdfMarkerRenderer(final MarkerStyle style) {
      super(PdfViewRenderer.this, style);
      this.minX = PdfViewRenderer.this.boundingBox.getMinX();
      this.minY = PdfViewRenderer.this.boundingBox.getMinY();
      this.modelUnitsPerViewUnit = getModelUnitsPerViewUnit();
    }

    @Override
    protected void renderMarkerDo() {
      try {
        final PDPageContentStream contentStream = PdfViewRenderer.this.contentStream;
        contentStream.saveGraphicsState();
        contentStream.transform(this.matrix);
        renderMarkerDo(contentStream);

        if (this.fill) {
          if (this.stroke) {
            contentStream.fillAndStroke();
          } else {
            contentStream.fill();
          }
        } else {
          if (this.stroke) {
            contentStream.stroke();
          }
        }

        contentStream.restoreGraphicsState();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }

    protected void renderMarkerDo(final PDPageContentStream contentStream) throws IOException {

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void translateDo(final double x, final double y, final double orientation,
      final double dx, final double dy) {
      final float viewX = (float)((x - this.minX) / this.modelUnitsPerViewUnit);
      final float viewY = (float)((y - this.minY) / this.modelUnitsPerViewUnit);

      this.matrix.reset();
      this.matrix.translate(viewX, viewY);
      if (orientation != 0) {
        this.matrix.rotate(Math.toRadians(360 - orientation));
      }
      this.matrix.translate((float)dx, (float)dy);
    }
  }

  private class PdfMarkerRendererEllipse extends PdfMarkerRenderer {

    public PdfMarkerRendererEllipse(final MarkerStyle style) {
      super(style);
    }

    @Override
    protected void renderMarkerDo() {
    }
  }

  private class PdfMarkerRendererGeometry extends PdfMarkerRenderer {
    private final Geometry geometry;

    public PdfMarkerRendererGeometry(final GeometryMarker marker, final MarkerStyle style) {
      super(style);
      this.geometry = marker.newMarker(this.mapWidth, this.mapHeight);
    }

    @Override
    protected void renderMarkerDo(final PDPageContentStream contentStream) throws IOException {
      renderMarkerGeometryDo(contentStream, this.geometry);
    }

    private void renderMarkerGeometryDo(final PDPageContentStream contentStream,
      final Geometry geometry) throws IOException {

      if (geometry.isGeometryCollection()) {
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          renderMarkerGeometryDo(contentStream, part);
        }
      } else if (geometry instanceof LineString) {
        final LineString line = (LineString)geometry;
        drawLine(contentStream, line);
      } else if (geometry instanceof Polygon) {
        final Polygon polygon = (Polygon)geometry;
        drawPolygon(contentStream, polygon);
      }
    }
  }

  private class PdfMarkerRendererImage extends PdfMarkerRenderer {
    private final Image image;

    public PdfMarkerRendererImage(final ImageMarker imageMarker, final MarkerStyle style) {
      super(style);
      this.image = imageMarker.getImage();
    }

    @Override
    protected void renderMarkerDo() {
      if (this.image != null) {
        // TODO renderMarkerImage(this.image, this.mapWidth, this.mapHeight);
      }
    }
  }

  private class PdfMarkerRendererRectangle extends PdfMarkerRenderer {

    public PdfMarkerRendererRectangle(final MarkerStyle style) {
      super(style);
    }

    @Override
    protected void renderMarkerDo(final PDPageContentStream contentStream) throws IOException {
      contentStream.addRect(0, 0, (float)this.mapWidth, (float)this.mapHeight);
    }
  }

  private class PdfMarkerRendererSvg extends PdfMarkerRenderer {

    private final PDImageXObject pdfImage;

    public PdfMarkerRendererSvg(final SvgMarker marker, final MarkerStyle style) {
      super(style);
      final Document document = marker.getDocument();
      if (document == null) {
        this.pdfImage = null;
      } else {
        final String uri = marker.getUri();
        final BufferedImage bufferedImage = SvgBufferedImageTranscoder.newImage(document, uri,
          (int)Math.round(this.mapWidth), (int)Math.round(this.mapHeight));

        try {
          this.pdfImage = LosslessFactory.createFromImage(PdfViewRenderer.this.document,
            bufferedImage);
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    }

    @Override
    protected void renderMarkerDo() {
      if (this.pdfImage != null) {
        try {
          PdfViewRenderer.this.contentStream.drawImage(this.pdfImage, 0f, 0f);
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    }
  }

  private class PdfMarkerRendererText extends PdfMarkerRenderer {

    // private final Font font;
    //
    // private final String text;

    public PdfMarkerRendererText(final TextMarker textMarker, final MarkerStyle style) {
      super(style);
      // final int fontSize = (int)this.mapHeight;
      // this.font = Fonts.newFont(textMarker.getTextFaceName(), 0, fontSize);
      // this.text = textMarker.getText();
    }

    @Override
    public void renderMarker(final double modelX, final double modelY, final double orientation) {
      // final MarkerStyle style = this.style;
      // // TODO
      // final Graphics2D graphics = getGraphics();
      // try (
      // BaseCloseable transformCloseable = useViewCoordinates()) {
      // final String orientationType = style.getMarkerOrientationType();
      // if ("none".equals(orientationType)) {
      // orientation = 0;
      // }
      //
      // final FontRenderContext fontRenderContext =
      // graphics.getFontRenderContext();
      // final GlyphVector glyphVector =
      // this.font.createGlyphVector(fontRenderContext, this.text);
      // final Shape shape = glyphVector.getOutline();
      // final GeneralPath newShape = new GeneralPath(shape);
      // final Rectangle2D bounds = newShape.getBounds2D();
      // final double shapeWidth = bounds.getWidth();
      // final double shapeHeight = bounds.getHeight();
      //
      // translateModelToViewCoordinates(modelX, modelY);
      // final double markerOrientation = style.getMarkerOrientation();
      // orientation = -orientation + markerOrientation;
      // if (orientation != 0) {
      // graphics.rotate(Math.toRadians(orientation));
      // }
      //
      // final Quantity<Length> deltaX = style.getMarkerDx();
      // final Quantity<Length> deltaY = style.getMarkerDy();
      // double dx = toDisplayValue(deltaX);
      // double dy = toDisplayValue(deltaY);
      // dy -= bounds.getY();
      // final String verticalAlignment = style.getMarkerVerticalAlignment();
      // if ("bottom".equals(verticalAlignment)) {
      // dy -= shapeHeight;
      // } else if ("auto".equals(verticalAlignment) ||
      // "middle".equals(verticalAlignment)) {
      // dy -= shapeHeight / 2.0;
      // }
      // final String horizontalAlignment =
      // style.getMarkerHorizontalAlignment();
      // if ("right".equals(horizontalAlignment)) {
      // dx -= shapeWidth;
      // } else if ("auto".equals(horizontalAlignment) ||
      // "center".equals(horizontalAlignment)) {
      // dx -= shapeWidth / 2;
      // }
      // graphics.translate(dx, dy);
      //
      // if (style.setMarkerFillStyle(PdfViewRenderer.this, graphics)) {
      //
      // graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      // RenderingHints.VALUE_ANTIALIAS_ON);
      // graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      // RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      // graphics.setFont(this.font);
      // graphics.drawString(this.text, 0, 0);
      // }
      // }
    }
  }

  private class PdfMarkerStyleCloseable implements BaseCloseable {

    @Override
    public void close() {
      try {
        PdfViewRenderer.this.contentStream.restoreGraphicsState();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }

    public BaseCloseable reset(final MarkerStyle style) {
      try {
        final PDPageContentStream contentStream = PdfViewRenderer.this.contentStream;
        contentStream.saveGraphicsState();
        PDExtendedGraphicsState graphicsState = PdfViewRenderer.this.graphicsStateByMarkerStyle
          .get(style);
        if (graphicsState == null) {
          graphicsState = new PDExtendedGraphicsState();

          final int lineOpacity = style.getMarkerLineOpacity();
          if (lineOpacity != 255) {
            graphicsState.setStrokingAlphaConstant(lineOpacity / 255f);
          }

          final Quantity<Length> lineWidth = style.getMarkerLineWidth();
          graphicsState.setLineWidth((float)toDisplayValue(lineWidth));

          final int polygonFillOpacity = style.getMarkerFillOpacity();
          if (polygonFillOpacity != 255) {
            graphicsState.setNonStrokingAlphaConstant(polygonFillOpacity / 255f);
          }

          PdfViewRenderer.this.graphicsStateByMarkerStyle.put(style, graphicsState);
        }
        contentStream.setGraphicsStateParameters(graphicsState);
        contentStream.setStrokingColor(style.getMarkerLineColor());
        contentStream.setNonStrokingColor(style.getMarkerFill());
        return this;
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  private final Canvas canvas = new Canvas();

  private final PDPageContentStream contentStream;

  private final double[] coordinates = new double[2];

  private final PDDocument document;

  private final Map<GeometryStyle, PDExtendedGraphicsState> graphicsStateByGeometryStyle = new HashMap<>();

  private final Map<MarkerStyle, PDExtendedGraphicsState> graphicsStateByMarkerStyle = new HashMap<>();

  private final PdfMarkerStyleCloseable markerStyleCloseable = new PdfMarkerStyleCloseable();

  private final Map<Layer, PDOptionalContentGroup> optionalContentGroups = new HashMap<>();

  private boolean useViewCoordinates;

  private final BaseCloseable useViewCoordinatesCloseable = () -> {
    this.useViewCoordinates = false;
  };

  public PdfViewRenderer(final PdfViewport viewport, final PDPageContentStream contentStream) {
    super(viewport);
    this.contentStream = contentStream;
    this.document = viewport.getDocument();
    setShowHiddenRecords(true);
    setBackgroundDrawingEnabled(false);
  }

  public void addPDOptionalContentGroup(final Layer layer, final PDOptionalContentGroup group) {
    this.optionalContentGroups.put(layer, group);
  }

  @Override
  public BaseCloseable applyMarkerStyle(final MarkerStyle style) {
    return this.markerStyleCloseable.reset(style);
  }

  @Override
  public void drawGeometry(final Geometry geometry, final GeometryStyle style) {
    final PDPageContentStream contentStream = this.contentStream;
    try {
      contentStream.saveGraphicsState();
      setGeometryStyle(style);
      contentStream.setNonStrokingColor(style.getPolygonFill());
      contentStream.setStrokingColor(style.getLineColor());

      for (Geometry part : geometry.geometries()) {
        part = part.convertGeometry(getGeometryFactory());
        if (part instanceof LineString) {
          final LineString line = (LineString)part;

          drawLine(contentStream, line);
          contentStream.stroke();
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;

          drawPolygon(contentStream, polygon);
          contentStream.fillAndStroke();
        }
      }

    } catch (final IOException e) {
    } finally {
      try {
        contentStream.restoreGraphicsState();
      } catch (final IOException e) {
      }
    }
  }

  @Override
  public void drawGeometryOutline(final GeometryStyle style, final Geometry geometry) {
    final PDPageContentStream contentStream = this.contentStream;
    try {
      contentStream.saveGraphicsState();
      setGeometryStyle(style);
      contentStream.setNonStrokingColor(style.getPolygonFill());
      contentStream.setStrokingColor(style.getLineColor());

      for (Geometry part : geometry.geometries()) {
        part = part.convertGeometry(getGeometryFactory());
        if (part instanceof LineString) {
          final LineString line = (LineString)part;

          drawLine(contentStream, line);
          contentStream.stroke();
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;

          for (final LinearRing ring : polygon.rings()) {
            drawLine(contentStream, ring);
          }
          contentStream.stroke();
        }
      }
    } catch (final IOException e) {
    } finally {
      try {
        contentStream.restoreGraphicsState();
      } catch (final IOException e) {
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
      try {
        final PDPageContentStream contentStream = this.contentStream;
        contentStream.saveGraphicsState();
        final PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
        graphicsState.setNonStrokingAlphaConstant((float)alpha);
        contentStream.setGraphicsStateParameters(graphicsState);

        drawImage(image, useTransform, interpolationMethod);

        contentStream.restoreGraphicsState();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
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
        image.drawImage(this, (renderedImage, imageBoundingBox, geoTransform) -> {
          try {
            final int imageWidth = renderedImage.getWidth();
            final int imageHeight = renderedImage.getHeight();
            final GeometryFactory viewGeometryFactory = viewBoundingBox.getGeometryFactory();
            imageBoundingBox = imageBoundingBox.bboxToCs(viewGeometryFactory);
            final double scaleFactor = viewWidth / viewBoundingBox.getWidth();

            final double imageMinX = imageBoundingBox.getMinX();
            final double viewMinX = viewBoundingBox.getMinX();
            final double screenX = (imageMinX - viewMinX) * scaleFactor;

            final double imageMinY = imageBoundingBox.getMinY();
            final double viewMinY = viewBoundingBox.getMinY();
            final double screenY = (imageMinY - viewMinY) * scaleFactor;

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
              final Matrix matrix = new Matrix(imageTransform);
              final PDImageXObject pdfImage = LosslessFactory.createFromImage(this.document,
                (BufferedImage)renderedImage);
              final PDPageContentStream contentStream = this.contentStream;
              contentStream.saveGraphicsState();
              contentStream.transform(matrix);
              contentStream.drawImage(pdfImage, 0f, 0f);
              contentStream.restoreGraphicsState();
            }

          } catch (final IOException e) {
            throw Exceptions.wrap(e);
          }
        }, viewBoundingBox, viewWidth, viewHeight, useTransform);
      }
    }
  }

  private void drawLine(final PDPageContentStream contentStream, final LineString line)
    throws IOException {
    for (int i = 0; i < line.getVertexCount(); i++) {
      final double modelX = line.getX(i);
      final double modelY = line.getY(i);
      final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
      final float viewX = (float)viewCoordinates[0];
      final float viewY = (float)viewCoordinates[1];
      if (i == 0) {
        contentStream.moveTo(viewX, viewY);
      } else {
        contentStream.lineTo(viewX, viewY);
      }
    }
  }

  private void drawLineReverse(final PDPageContentStream contentStream, final LineString line)
    throws IOException {
    final int toVertexIndex = line.getVertexCount() - 1;
    for (int i = toVertexIndex; i >= 0; i--) {
      final double modelX = line.getX(i);
      final double modelY = line.getY(i);
      final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
      final float viewX = (float)viewCoordinates[0];
      final float viewY = (float)viewCoordinates[1];
      if (i == toVertexIndex) {
        contentStream.moveTo(viewX, viewY);
      } else {
        contentStream.lineTo(viewX, viewY);
      }
    }
  }

  @Override
  public void drawLines(final GeometryStyle style, final Collection<LineString> lines) {
    if (!lines.isEmpty()) {
      final PDPageContentStream contentStream = this.contentStream;
      try {
        contentStream.saveGraphicsState();
        setGeometryStyle(style);
        contentStream.setStrokingColor(style.getLineColor());

        for (final LineString line : lines) {
          drawLine(contentStream, line);
          contentStream.stroke();
        }
        contentStream.restoreGraphicsState();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
  }

  private void drawPolygon(final PDPageContentStream contentStream, final Polygon polygon)
    throws IOException {
    int i = 0;
    for (final LinearRing ring : polygon.rings()) {
      if (i == 0) {
        if (ring.isClockwise()) {
          drawLineReverse(contentStream, ring);
        } else {
          drawLine(contentStream, ring);
        }
      } else {
        if (ring.isCounterClockwise()) {
          drawLineReverse(contentStream, ring);
        } else {
          drawLine(contentStream, ring);
        }
      }
      // contentStream.closePath();
      i++;
    }
  }

  @Override
  public void drawText(final Record record, final Geometry geometry, final TextStyle style) {
    try {
      final String label = style.getLabel(record);
      if (Property.hasValue(label) && geometry != null) {
        final String textPlacementType = style.getTextPlacementType();
        final PointDoubleXYOrientation point = AbstractRecordLayerRenderer
          .getPointWithOrientation(this, geometry, textPlacementType);
        if (point != null) {
          final double orientation = point.getOrientation();

          final PDPageContentStream contentStream = this.contentStream;
          contentStream.saveGraphicsState();
          try {
            // style.setTextStyle(viewport, graphics);

            final double x = point.getX();
            final double y = point.getY();
            final double modelUnitsPerViewUnit = getModelUnitsPerViewUnit();
            double viewX = (x - this.boundingBox.getMinX()) / modelUnitsPerViewUnit;
            double viewY = (y - this.boundingBox.getMinY()) / modelUnitsPerViewUnit;

            // style.setTextStyle(viewport, graphics);

            final Quantity<Length> textDx = style.getTextDx();
            float dx = (float)this.toDisplayValue(textDx);

            final Quantity<Length> textDy = style.getTextDy();
            float dy = (float)this.toDisplayValue(textDy);
            final Font font = style.getFont(this);
            final FontMetrics fontMetrics = this.canvas.getFontMetrics(font);

            double maxWidth = 0;
            final String[] lines = label.split("[\\r\\n]");
            for (final String line : lines) {
              final Rectangle2D bounds = fontMetrics.getStringBounds(line,
                this.canvas.getGraphics());
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
              dy -= maxHeight;
            } else if ("middle".equals(verticalAlignment)) {
              dy -= maxHeight / 2;
            } else {
            }

            String horizontalAlignment = style.getTextHorizontalAlignment();
            final String textPlacement = style.getTextPlacementType();
            if ("auto".equals(textPlacement)) {
              if (viewX < 0) {
                viewX = 1;
                dx = 0;
                horizontalAlignment = "left";
              }
              final double viewWidth = getViewWidthPixels();
              if (viewX + maxWidth > viewWidth) {
                viewX = (int)(viewWidth - maxWidth - 1);
                dx = 0;
                horizontalAlignment = "left";
              }
              if (viewY < maxHeight) {
                viewY = 1;
                dy = 0;
              }
              final double viewHeight = getViewHeightPixels();
              if (viewY > viewHeight) {
                viewY = viewHeight - 1 - maxHeight;
                dy = 0;
              }
            }
            Matrix transform = Matrix.getTranslateInstance((float)viewX, (float)viewY);
            if (orientation != 0) {
              transform.rotate((float)Math.toRadians(orientation));
            }
            transform.translate(dx, dy);

            for (final String line : lines) {
              // transform.translate(0, ascent);
              final Matrix lineTransform = transform.clone();
              final Rectangle2D bounds = fontMetrics.getStringBounds(line,
                this.canvas.getGraphics());
              final float width = (float)bounds.getWidth();
              final float height = (float)bounds.getHeight();

              if ("right".equals(horizontalAlignment)) {
                transform.translate(-width, 0f);
              } else if ("center".equals(horizontalAlignment)
                || "auto".equals(horizontalAlignment)) {
                transform.translate(-width / 2, 0f);
              }
              transform.translate(dx, 0);

              transform.scale(1, 1);
              if (Math.abs(orientation) > 90) {
                final float anchorX = (float)maxWidth / 2;
                final float anchorY = -height / 4;
                transform.translate(anchorX, anchorY);
                transform.rotate(Math.PI);
                transform.translate(-anchorX, -anchorY);
              }
              /*
               * final double textHaloRadius = Viewport2D.toDisplayValue(this,
               * style.getTextHaloRadius()); if (textHaloRadius > 0) {
               * graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
               * RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB); final Stroke
               * savedStroke = graphics.getStroke(); final Stroke outlineStroke
               * = new BasicStroke( (float)textHaloRadius, BasicStroke.CAP_BUTT,
               * BasicStroke.JOIN_BEVEL);
               * graphics.setColor(style.getTextHaloFill());
               * graphics.setStroke(outlineStroke); final Font font =
               * graphics.getFont(); final FontRenderContext fontRenderContext =
               * graphics.getFontRenderContext(); final TextLayout textLayout =
               * new TextLayout(ring, font, fontRenderContext); final Shape
               * outlineShape =
               * textLayout.getOutline(TextStyleRenderer.NOOP_TRANSFORM);
               * graphics.draw(outlineShape); graphics.setStroke(savedStroke); }
               */
              final Color textBoxColor = style.getTextBoxColor();
              if (textBoxColor != null) {
                // contentStream.transform(transform);
                // contentStream.setNonStrokingColor(textBoxColor);
                // contentStream.addRect((float)bounds.getX() - 3,
                // (float)bounds.getY() - 1, width + 6,
                // height + 2);
                // contentStream.fill();
              }
              contentStream.setNonStrokingColor(style.getTextFill());

              contentStream.beginText();
              final PDFont pdfFont = PDType1Font.HELVETICA;

              contentStream.setFont(pdfFont, font.getSize2D());
              contentStream.setTextMatrix(transform);
              contentStream.showText(line);
              contentStream.endText();

              transform = lineTransform;
              transform.translate(0, leading + descent);
            }

          } finally {
            contentStream.restoreGraphicsState();
          }
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to write PDF", e);
    }
  }

  @Override
  public void fillPolygons(final GeometryStyle style, final Collection<Polygon> polygons) {
    if (!polygons.isEmpty()) {
      final PDPageContentStream contentStream = this.contentStream;
      try {
        contentStream.saveGraphicsState();
        setGeometryStyle(style);
        contentStream.setNonStrokingColor(style.getPolygonFill());

        for (final Polygon polygon : polygons) {
          drawPolygon(contentStream, polygon);
        }
        contentStream.fill();
      } catch (final IOException e) {
      } finally {
        try {
          contentStream.restoreGraphicsState();
        } catch (final IOException e) {
        }
      }
    }
  }

  public Canvas getCanvas() {
    return this.canvas;
  }

  public PDPageContentStream getContentStream() {
    return this.contentStream;
  }

  public final PDFont getFont(final String path) throws IOException {
    return ((PdfViewport)this.viewport).getFont(path);
  }

  public PDOptionalContentGroup getPDOptionalContentGroup(final Layer layer) {
    return this.optionalContentGroups.get(layer);
  }

  @Override
  public boolean isHidden(final AbstractRecordLayer layer, final LayerRecord record) {
    return false;
  }

  @Override
  public MarkerRenderer newMarkerRendererEllipse(final MarkerStyle style) {
    return new PdfMarkerRendererEllipse(style);
  }

  @Override
  public MarkerRenderer newMarkerRendererGeometry(final GeometryMarker geometryMarker,
    final MarkerStyle style) {
    return new PdfMarkerRendererGeometry(geometryMarker, style);
  }

  @Override
  public MarkerRenderer newMarkerRendererImage(final ImageMarker imageMarker,
    final MarkerStyle style) {
    return new PdfMarkerRendererImage(imageMarker, style);
  }

  @Override
  public MarkerRenderer newMarkerRendererRectangle(final MarkerStyle style) {
    return new PdfMarkerRendererRectangle(style);
  }

  @Override
  public MarkerRenderer newMarkerRendererSvg(final SvgMarker svgMarker, final MarkerStyle style) {
    return new PdfMarkerRendererSvg(svgMarker, style);
  }

  @Override
  public MarkerRenderer newMarkerRendererText(final TextMarker textMarker,
    final MarkerStyle style) {
    return new PdfMarkerRendererText(textMarker, style);
  }

  @Override
  public TextStyleViewRenderer newTextStyleViewRenderer(final TextStyle textStyle) {
    return new PdfTextStyleRenderer(this, textStyle);
  }

  @Override
  protected void renderLayerDo(final Layer layer, final LayerRenderer<?> renderer) {
    try {

      final PDOptionalContentGroup contentGroup = getPDOptionalContentGroup(layer);

      if (contentGroup != null) {
        this.contentStream.beginMarkedContent(COSName.OC, contentGroup);
      }
      renderer.render(this);
      if (contentGroup != null) {
        this.contentStream.endMarkedContent();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private void setGeometryStyle(final GeometryStyle style) throws IOException {
    PDExtendedGraphicsState graphicsState = this.graphicsStateByGeometryStyle.get(style);
    if (graphicsState == null) {
      graphicsState = new PDExtendedGraphicsState();

      final int lineOpacity = style.getLineOpacity();
      if (lineOpacity != 255) {
        graphicsState.setStrokingAlphaConstant(lineOpacity / 255f);
      }

      final Quantity<Length> lineWidth = style.getLineWidth();
      final Unit<Length> unit = lineWidth.getUnit();
      graphicsState.setLineWidth((float)toDisplayValue(lineWidth));

      final List<Double> lineDashArray = style.getLineDashArray();
      if (lineDashArray != null && !lineDashArray.isEmpty()) {
        int size = lineDashArray.size();
        if (size == 1) {
          size++;
        }
        final float[] dashArray = new float[size];

        for (int i = 0; i < dashArray.length; i++) {
          if (i < lineDashArray.size()) {
            final Double dashDouble = lineDashArray.get(i);
            final Quantity<Length> dashMeasure = Quantities.getQuantity(dashDouble, unit);
            final float dashFloat = (float)toDisplayValue(dashMeasure);
            dashArray[i] = dashFloat;
          } else {
            dashArray[i] = dashArray[i - 1];
          }
        }
        // TODO dash array disabled due to bug in PDFbox
        // final int offset = (int)toDisplayValue(
        // Quantities.getQuantity(style.getLineDashOffset(), unit));
        // final COSArray dashCosArray = new COSArray();
        // dashCosArray.setFloatArray(dashArray);
        // final PDLineDashPattern pattern = new PDLineDashPattern(dashCosArray,
        // offset);
        // graphicsState.setLineDashPattern(pattern);
      }
      switch (style.getLineCap()) {
        case BUTT:
          graphicsState.setLineCapStyle(0);
        break;
        case ROUND:
          graphicsState.setLineCapStyle(1);
        break;
        case SQUARE:
          graphicsState.setLineCapStyle(2);
        break;
      }

      switch (style.getLineJoin()) {
        case MITER:
          graphicsState.setLineJoinStyle(0);
        break;
        case ROUND:
          graphicsState.setLineJoinStyle(1);
        break;
        case BEVEL:
          graphicsState.setLineJoinStyle(2);
        break;
      }

      final int polygonFillOpacity = style.getPolygonFillOpacity();
      if (polygonFillOpacity != 255) {
        graphicsState.setNonStrokingAlphaConstant(polygonFillOpacity / 255f);
      }

      this.graphicsStateByGeometryStyle.put(style, graphicsState);
    }
    this.contentStream.setGraphicsStateParameters(graphicsState);
  }

  public double[] toViewCoordinates(final double x, final double y) {
    final double[] coordinates = this.coordinates;
    if (this.useViewCoordinates) {
      coordinates[0] = x;
      coordinates[1] = y;
    } else {
      final double modelUnitsPerViewUnit = getModelUnitsPerViewUnit();
      final double viewX = (x - this.boundingBox.getMinX()) / modelUnitsPerViewUnit;
      final double viewY = (y - this.boundingBox.getMinY()) / modelUnitsPerViewUnit;
      coordinates[0] = viewX;
      coordinates[1] = viewY;
    }
    return coordinates;
  }

  @Override
  public BaseCloseable useViewCoordinates() {
    this.useViewCoordinates = true;
    return this.useViewCoordinatesCloseable;
  }

}

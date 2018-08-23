package com.revolsys.swing.map.view.pdf;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.view.TextStyleViewRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;

import tec.uom.se.quantity.Quantities;

public class PdfViewRenderer extends ViewRenderer {

  private final PDPageContentStream contentStream;

  private final Canvas canvas = new Canvas();

  private int styleId = 0;

  private final Map<GeometryStyle, String> styleNames = new HashMap<>();

  private final PDPage page;

  public PdfViewRenderer(final PdfViewport viewport, final PDPageContentStream contentStream) {
    super(viewport);
    this.contentStream = contentStream;
    this.page = viewport.getPage();
    updateFields();
  }

  @Override
  public void drawGeometry(final Geometry geometry, final GeometryStyle style) {
    try {
      this.contentStream.saveGraphicsState();
      setGeometryStyle(style);
      this.contentStream.setNonStrokingColor(style.getPolygonFill());
      this.contentStream.setStrokingColor(style.getLineColor());

      for (Geometry part : geometry.geometries()) {
        part = part.convertGeometry(getGeometryFactory());
        if (part instanceof LineString) {
          final LineString line = (LineString)part;

          drawLine(line);
          this.contentStream.stroke();
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;

          int i = 0;
          for (final LinearRing ring : polygon.rings()) {
            if (i == 0) {
              if (ring.isClockwise()) {
                drawLineReverse(ring);
              } else {
                drawLine(ring);
              }
            } else {
              if (ring.isCounterClockwise()) {
                drawLineReverse(ring);
              } else {
                drawLine(ring);
              }
            }
            this.contentStream.closeSubPath();
            i++;
          }
          this.contentStream.fill(PathIterator.WIND_NON_ZERO);
          for (final LinearRing ring : polygon.rings()) {

            drawLine(ring);
            this.contentStream.stroke();
          }
        }
      }

    } catch (final IOException e) {
    } finally {
      try {
        this.contentStream.restoreGraphicsState();
      } catch (final IOException e) {
      }
    }
  }

  @Override
  public void drawGeometryOutline(final Geometry geometry, final GeometryStyle style) {
    try {
      this.contentStream.saveGraphicsState();
      setGeometryStyle(style);
      this.contentStream.setNonStrokingColor(style.getPolygonFill());
      this.contentStream.setStrokingColor(style.getLineColor());

      for (Geometry part : geometry.geometries()) {
        part = part.convertGeometry(getGeometryFactory());
        if (part instanceof LineString) {
          final LineString line = (LineString)part;

          drawLine(line);
          this.contentStream.stroke();
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;

          int i = 0;
          for (final LinearRing ring : polygon.rings()) {
            if (i == 0) {
              if (ring.isClockwise()) {
                drawLineReverse(ring);
              } else {
                drawLine(ring);
              }
            } else {
              if (ring.isCounterClockwise()) {
                drawLineReverse(ring);
              } else {
                drawLine(ring);
              }
            }
            this.contentStream.closeSubPath();
            i++;
          }
          for (final LinearRing ring : polygon.rings()) {

            drawLine(ring);
            this.contentStream.stroke();
          }
        }
      }

    } catch (final IOException e) {
    } finally {
      try {
        this.contentStream.restoreGraphicsState();
      } catch (final IOException e) {
      }
    }
  }

  @Override
  public void drawImage(final GeoreferencedImage image, final boolean useTransform) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawImage(final GeoreferencedImage image, final boolean useTransform,
    final double alpha, final Object interpolationMethod) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawImage(final GeoreferencedImage image, final boolean useTransform,
    final Object interpolationMethod) {
    // TODO Auto-generated method stub
  }

  private void drawLine(final LineString line) throws IOException {
    for (int i = 0; i < line.getVertexCount(); i++) {
      final double modelX = line.getX(i);
      final double modelY = line.getY(i);
      final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
      final float viewX = (float)viewCoordinates[0];
      final float viewY = (float)(getViewHeightPixels() - viewCoordinates[1]);
      if (i == 0) {
        this.contentStream.moveTo(viewX, viewY);
      } else {
        this.contentStream.lineTo(viewX, viewY);
      }
    }
  }

  private void drawLineReverse(final LineString line) throws IOException {
    final int toVertexIndex = line.getVertexCount() - 1;
    for (int i = toVertexIndex; i >= 0; i--) {
      final double modelX = line.getX(i);
      final double modelY = line.getY(i);
      final double[] viewCoordinates = toViewCoordinates(modelX, modelY);
      final float viewX = (float)viewCoordinates[0];
      final float viewY = (float)(getViewHeightPixels() - viewCoordinates[1]);
      if (i == toVertexIndex) {
        this.contentStream.moveTo(viewX, viewY);
      } else {
        this.contentStream.lineTo(viewX, viewY);
      }
    }
  }

  @Override
  public void drawMarker(final Point point, final MarkerStyle style, final double orientation) {
    // TODO Auto-generated method stub
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

          this.contentStream.saveGraphicsState();
          try {
            // style.setTextStyle(viewport, graphics);

            final double x = point.getX();
            final double y = point.getY();
            final double[] location = toViewCoordinates(x, y);

            // style.setTextStyle(viewport, graphics);

            final Quantity<Length> textDx = style.getTextDx();
            double dx = this.toDisplayValue(textDx);

            final Quantity<Length> textDy = style.getTextDy();
            double dy = -this.toDisplayValue(textDy);
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
            } else if ("middle".equals(verticalAlignment)) {
              dy -= maxHeight / 2;
            } else {
              dy -= maxHeight;
            }

            String horizontalAlignment = style.getTextHorizontalAlignment();
            double screenX = location[0];
            double screenY = getViewHeightPixels() - location[1];
            final String textPlacement = style.getTextPlacementType();
            if ("auto".equals(textPlacement)) {
              if (screenX < 0) {
                screenX = 1;
                dx = 0;
                horizontalAlignment = "left";
              }
              final int viewWidth = getViewWidthPixels();
              if (screenX + maxWidth > viewWidth) {
                screenX = (int)(viewWidth - maxWidth - 1);
                dx = 0;
                horizontalAlignment = "left";
              }
              if (screenY < maxHeight) {
                screenY = 1;
                dy = 0;
              }
              final int viewHeight = getViewHeightPixels();
              if (screenY > viewHeight) {
                screenY = viewHeight - 1 - maxHeight;
                dy = 0;
              }
            }
            AffineTransform transform = new AffineTransform();
            transform.translate(screenX, screenY);
            if (orientation != 0) {
              transform.rotate(-Math.toRadians(orientation), 0, 0);
            }
            transform.translate(dx, dy);

            for (final String line : lines) {
              transform.translate(0, ascent);
              final AffineTransform lineTransform = new AffineTransform(transform);
              final Rectangle2D bounds = fontMetrics.getStringBounds(line,
                this.canvas.getGraphics());
              final double width = bounds.getWidth();
              final double height = bounds.getHeight();

              if ("right".equals(horizontalAlignment)) {
                transform.translate(-width, 0);
              } else if ("center".equals(horizontalAlignment)) {
                transform.translate(-width / 2, 0);
              }
              transform.translate(dx, 0);

              transform.scale(1, 1);
              if (Math.abs(orientation) > 90) {
                transform.rotate(Math.PI, maxWidth / 2, -height / 4);
              }
              /*
               * final double textHaloRadius = Viewport2D.toDisplayValue(this,
               * style.getTextHaloRadius()); if (textHaloRadius > 0) {
               * graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
               * RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB); final Stroke savedStroke =
               * graphics.getStroke(); final Stroke outlineStroke = new BasicStroke(
               * (float)textHaloRadius, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
               * graphics.setColor(style.getTextHaloFill()); graphics.setStroke(outlineStroke);
               * final Font font = graphics.getFont(); final FontRenderContext fontRenderContext =
               * graphics.getFontRenderContext(); final TextLayout textLayout = new TextLayout(line,
               * font, fontRenderContext); final Shape outlineShape =
               * textLayout.getOutline(TextStyleRenderer.NOOP_TRANSFORM);
               * graphics.draw(outlineShape); graphics.setStroke(savedStroke); }
               */
              final Color textBoxColor = style.getTextBoxColor();
              if (textBoxColor != null) {
                this.contentStream.setNonStrokingColor(textBoxColor);
                final double cornerSize = Math.max(height / 2, 5);
                // final RoundRectangle2D.Double box = new
                // RoundRectangle2D.Double(
                // bounds.getX() - 3, bounds.getY() - 1, width + 6, height + 2,
                // cornerSize, cornerSize);
                this.contentStream.fillRect((float)bounds.getX() - 3, (float)bounds.getY() - 1,
                  (float)width + 6, (float)height + 2);
              }
              this.contentStream.setNonStrokingColor(style.getTextFill());

              this.contentStream.beginText();
              final PDFont pdfFont = getViewport()
                .getFont("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");

              this.contentStream.setFont(pdfFont, font.getSize2D());
              this.contentStream.setTextMatrix(transform);
              this.contentStream.drawString(line);
              this.contentStream.endText();

              transform = lineTransform;
              transform.translate(0, leading + descent);
            }

          } finally {
            this.contentStream.restoreGraphicsState();
          }
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to write PDF", e);
    }
  }

  public Canvas getCanvas() {
    return this.canvas;
  }

  public PDPageContentStream getContentStream() {
    return this.contentStream;
  }

  @Override
  public PdfViewport getViewport() {
    return (PdfViewport)super.getViewport();
  }

  @Override
  public TextStyleViewRenderer newTextStyleViewRenderer(final TextStyle textStyle) {
    return new PdfTextStyleRenderer(this, textStyle);
  }

  private void setGeometryStyle(final GeometryStyle style) throws IOException {
    String styleName = this.styleNames.get(style);
    if (styleName == null) {
      styleName = "rgStyle" + this.styleId++;

      final PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();

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
        final int offset = (int)toDisplayValue(
          Quantities.getQuantity(style.getLineDashOffset(), unit));
        final COSArray dashCosArray = new COSArray();
        dashCosArray.setFloatArray(dashArray);
        final PDLineDashPattern pattern = new PDLineDashPattern(dashCosArray, offset);
        graphicsState.setLineDashPattern(pattern);
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

      final PDResources resources = this.page.findResources();
      Map<String, PDExtendedGraphicsState> graphicsStateDictionary = resources.getGraphicsStates();
      if (graphicsStateDictionary == null) {
        graphicsStateDictionary = new TreeMap<>();
      }
      graphicsStateDictionary.put(styleName, graphicsState);
      resources.setGraphicsStates(graphicsStateDictionary);

      this.styleNames.put(style, styleName);
    }
    this.contentStream.appendRawCommands("/" + styleName + " gs\n");
  }

}

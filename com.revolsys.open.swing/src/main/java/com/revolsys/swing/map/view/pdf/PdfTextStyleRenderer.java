package com.revolsys.swing.map.view.pdf;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.view.TextStyleViewRenderer;
import com.revolsys.util.Property;

public class PdfTextStyleRenderer extends TextStyleViewRenderer {

  private final PdfViewRenderer view;

  private final PDPageContentStream contentStream;

  private final Canvas canvas;

  public PdfTextStyleRenderer(final PdfViewRenderer view, final TextStyle style) {
    super(style);
    this.view = view;
    this.contentStream = view.getContentStream();
    this.canvas = view.getCanvas();
  }

  @Override
  public void drawText(final String label, final Geometry geometry) {
    try {
      if (Property.hasValue(label) && geometry != null) {
        final String textPlacementType = this.style.getTextPlacementType();
        final PointDoubleXYOrientation point = this.view.getPointWithOrientation(geometry,
          textPlacementType);
        if (point != null) {
          final double orientation = point.getOrientation();

          final PDPageContentStream contentStream = this.contentStream;
          contentStream.saveGraphicsState();
          try {
            // style.setTextStyle(viewport, graphics);

            final double x = point.getX();
            final double y = point.getY();
            final double[] location = this.view.toViewCoordinates(x, y);

            // style.setTextStyle(viewport, graphics);

            final Quantity<Length> textDx = this.style.getTextDx();
            double dx = this.view.toDisplayValue(textDx);

            final Quantity<Length> textDy = this.style.getTextDy();
            double dy = -this.view.toDisplayValue(textDy);
            final Font font = this.style.getFont(this.view);
            final Canvas canvas = this.canvas;
            final FontMetrics fontMetrics = canvas.getFontMetrics(font);

            double maxWidth = 0;
            final String[] lines = label.split("[\\r\\n]");
            for (final String line : lines) {
              final Rectangle2D bounds = fontMetrics.getStringBounds(line, canvas.getGraphics());
              final double width = bounds.getWidth();
              maxWidth = Math.max(width, maxWidth);
            }
            final int descent = fontMetrics.getDescent();
            final int ascent = fontMetrics.getAscent();
            final int leading = fontMetrics.getLeading();
            final double maxHeight = lines.length * (ascent + descent)
              + (lines.length - 1) * leading;
            final String verticalAlignment = this.style.getTextVerticalAlignment();
            if ("top".equals(verticalAlignment)) {
            } else if ("middle".equals(verticalAlignment)) {
              dy -= maxHeight / 2;
            } else {
              dy -= maxHeight;
            }

            String horizontalAlignment = this.style.getTextHorizontalAlignment();
            double screenX = location[0];
            double screenY = this.view.getViewHeightPixels() - location[1];
            final String textPlacement = this.style.getTextPlacementType();
            if ("auto".equals(textPlacement)) {
              if (screenX < 0) {
                screenX = 1;
                dx = 0;
                horizontalAlignment = "left";
              }
              final double viewWidth = this.view.getViewWidthPixels();
              if (screenX + maxWidth > viewWidth) {
                screenX = (int)(viewWidth - maxWidth - 1);
                dx = 0;
                horizontalAlignment = "left";
              }
              if (screenY < maxHeight) {
                screenY = 1;
                dy = 0;
              }
              final double viewHeight = this.view.getViewHeightPixels();
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
              final Rectangle2D bounds = fontMetrics.getStringBounds(line, canvas.getGraphics());
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
              final Color textBoxColor = this.style.getTextBoxColor();
              if (textBoxColor != null) {
                contentStream.setNonStrokingColor(textBoxColor);
                final double cornerSize = Math.max(height / 2, 5);
                // final RoundRectangle2D.Double box = new
                // RoundRectangle2D.Double(
                // bounds.getX() - 3, bounds.getY() - 1, width + 6, height + 2,
                // cornerSize, cornerSize);
                contentStream.fillRect((float)bounds.getX() - 3, (float)bounds.getY() - 1,
                  (float)width + 6, (float)height + 2);
              }
              contentStream.setNonStrokingColor(this.style.getTextFill());

              contentStream.beginText();
              final PDFont pdfFont = this.view
                .getFont("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");

              contentStream.setFont(pdfFont, font.getSize2D());
              contentStream.setTextMatrix(transform);
              contentStream.drawString(line);
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

}

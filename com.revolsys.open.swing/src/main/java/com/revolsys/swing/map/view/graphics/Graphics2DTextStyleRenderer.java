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

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.view.TextStyleViewRenderer;
import com.revolsys.util.Property;

import tech.units.indriya.quantity.Quantities;

public class Graphics2DTextStyleRenderer extends TextStyleViewRenderer {

  private final Graphics2DViewRenderer view;

  private final Graphics2D graphics;

  private final double[] coordinates = new double[2];

  private final double dx;

  private final double dy;

  public Graphics2DTextStyleRenderer(final Graphics2DViewRenderer view, final TextStyle style) {
    super(style);
    this.view = view;
    this.graphics = view.getGraphics();

    final Quantity<Length> textDx = style.getTextDx();
    this.dx = view.toDisplayValue(textDx);

    final Quantity<Length> textDy = style.getTextDy();
    this.dy = -view.toDisplayValue(textDy);
  }

  @Override
  public void drawText(final String label, final Geometry geometry) {
    final Graphics2DViewRenderer view = this.view;
    double dx = this.dx;
    double dy = this.dy;
    if (Property.hasValue(label) && geometry != null) {
      final TextStyle style = this.style;
      final String textPlacementType = style.getTextPlacementType();
      final PointDoubleXYOrientation point = view.getPointWithOrientation(geometry,
        textPlacementType);
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

        final AffineTransform savedTransform = graphics.getTransform();
        try {
          graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
          graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

          final double[] coordinates = this.coordinates;
          point.copyCoordinates(coordinates);
          view.toViewCoordinates(coordinates);

          style.setTextStyle(view, graphics);

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
          final double maxHeight = lines.length * (ascent + descent) + (lines.length - 1) * leading;
          final String verticalAlignment = style.getTextVerticalAlignment();
          if ("top".equals(verticalAlignment)) {
          } else if ("middle".equals(verticalAlignment)) {
            dy -= maxHeight / 2;
          } else {
            dy -= maxHeight;
          }

          String horizontalAlignment = style.getTextHorizontalAlignment();
          double screenX = coordinates[0];
          double screenY = coordinates[1];
          final String textPlacement = textPlacementType;
          if ("auto".equals(textPlacement) && view != null) {
            if (screenX < 0) {
              screenX = 1;
              dx = 0;
              horizontalAlignment = "left";
            }
            final double viewWidth = view.getViewWidthPixels();
            if (screenX + maxWidth > viewWidth) {
              screenX = (int)(viewWidth - maxWidth - 1);
              dx = 0;
              horizontalAlignment = "left";
            }
            if (screenY < maxHeight) {
              screenY = 1;
              dy = 0;
            }
            final double viewHeight = view.getViewHeightPixels();
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
            } else if ("center".equals(horizontalAlignment) || "auto".equals(horizontalAlignment)) {
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
            final double textHaloRadius = view.toDisplayValue(Quantities.getQuantity(radius, unit));
            if (textHaloRadius > 0) {
              final Stroke savedStroke = graphics.getStroke();
              final Stroke outlineStroke = new BasicStroke((float)(textHaloRadius + 1),
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
              graphics.setColor(style.getTextHaloFill());
              graphics.setStroke(outlineStroke);
              final Font font = graphics.getFont();
              final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
              final TextLayout textLayout = new TextLayout(line, font, fontRenderContext);
              final Shape outlineShape = textLayout
                .getOutline(Graphics2DViewRenderer.IDENTITY_TRANSFORM);
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
          graphics.setTransform(savedTransform);
          graphics.setPaint(paint);
          graphics.setComposite(composite);
        }
      }
    }
  }

}

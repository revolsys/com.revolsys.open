package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.Fonts;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;

public class TextMarker extends AbstractMarker {
  private String text = "?";

  private Font font;

  private String textFaceName = "san-serif";

  public TextMarker(final Map<String, ? extends Object> config) {
    setProperties(config);
  }

  public TextMarker(final String textFaceName, final String text) {
    this.textFaceName = textFaceName;
    this.text = text;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof TextMarker) {
      final TextMarker marker = (TextMarker)object;
      if (getText().equals(marker.getText())) {
        return getFont().equals(marker.getFont());
      }
    }
    return false;
  }

  public Font getFont() {
    return this.font;
  }

  public String getText() {
    return this.text;
  }

  public String getTextFaceName() {
    return this.textFaceName;
  }

  @Override
  public String getTypeName() {
    return "markerText";
  }

  @Override
  public int hashCode() {
    return getText().hashCode();
  }

  @Override
  public Icon newIcon(final MarkerStyle style) {
    Icon icon;
    final BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();

    double orientation = style.getMarkerOrientation();

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    final String textFaceName = getTextFaceName();
    final Font font = Fonts.newFont(textFaceName, 0, 14);
    graphics.setFont(font);
    final FontMetrics fontMetrics = graphics.getFontMetrics();

    int x = 0;
    int y = 15;
    final String text = getText();
    final Rectangle2D bounds = fontMetrics.getStringBounds(text, graphics);
    final double width = bounds.getWidth();
    final double height = fontMetrics.getAscent();
    final String horizontalAlignment = style.getMarkerHorizontalAlignment();
    if ("right".equals(horizontalAlignment)) {
      x = 15 - (int)width;
    } else if ("center".equals(horizontalAlignment) || "auto".equals(horizontalAlignment)) {
      x = 8 - (int)(width / 2);
    }
    final String verticalAlignment = style.getMarkerVerticalAlignment();
    if ("top".equals(verticalAlignment)) {
      y = (int)height;
    } else if ("middle".equals(verticalAlignment) || "auto".equals(verticalAlignment)) {
      y = 7 + (int)(height / 2);
    }
    if (orientation != 0) {
      if (orientation > 270) {
        orientation -= 360;
      }
      graphics.rotate(-Math.toRadians(orientation), 8, 8);
    }

    graphics.setColor(style.getMarkerFill());
    graphics.drawString(text, x, y);
    graphics.dispose();
    icon = new ImageIcon(image);
    return icon;

  }

  @Override
  public void render(final Graphics2DViewRenderer view, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY, double orientation) {
    try (
      BaseCloseable transformCloseable = view.useViewCoordinates()) {
      final Quantity<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = view.toDisplayValue(markerHeight);
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }
      final int fontSize = (int)mapHeight;
      if (this.font == null || this.font.getSize() != fontSize) {
        this.font = Fonts.newFont(this.textFaceName, 0, fontSize);
      }

      final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
      final GlyphVector glyphVector = this.font.createGlyphVector(fontRenderContext, this.text);
      final Shape shape = glyphVector.getOutline();
      final GeneralPath newShape = new GeneralPath(shape);
      final Rectangle2D bounds = newShape.getBounds2D();
      final double shapeWidth = bounds.getWidth();
      final double shapeHeight = bounds.getHeight();

      view.translateModelToViewCoordinates(modelX, modelY);
      final double markerOrientation = style.getMarkerOrientation();
      orientation = -orientation + markerOrientation;
      if (orientation != 0) {
        graphics.rotate(Math.toRadians(orientation));
      }

      final Quantity<Length> deltaX = style.getMarkerDx();
      final Quantity<Length> deltaY = style.getMarkerDy();
      double dx = view.toDisplayValue(deltaX);
      double dy = view.toDisplayValue(deltaY);
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

      if (style.setMarkerFillStyle(view, graphics)) {

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        graphics.setFont(this.font);
        graphics.drawString(this.text, 0, 0);
      }
    }
  }

  public void setText(final String text) {
    final Object oldValue = this.text;
    this.text = MarkerStyle.getWithDefault(text, "?");
    this.font = null;
    firePropertyChange("text", oldValue, this.text);
  }

  public void setTextFaceName(final String textFaceName) {
    final Object oldValue = this.textFaceName;
    this.textFaceName = textFaceName;
    this.font = null;
    firePropertyChange("textFaceName", oldValue, this.textFaceName);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "textFaceName", this.textFaceName);
    addToMap(map, "text", this.text);
    return map;
  }

  @Override
  public String toString() {
    return getText();
  }
}

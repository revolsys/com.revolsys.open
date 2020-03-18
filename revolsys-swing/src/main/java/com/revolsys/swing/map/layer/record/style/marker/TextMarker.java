package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Fonts;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;

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
  public MarkerRenderer newMarkerRenderer(final ViewRenderer view, final MarkerStyle style) {
    return view.newMarkerRendererText(this, style);
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "textFaceName", this.textFaceName);
    addToMap(map, "text", this.text);
    return map;
  }

  @Override
  public String toString() {
    return getText();
  }
}

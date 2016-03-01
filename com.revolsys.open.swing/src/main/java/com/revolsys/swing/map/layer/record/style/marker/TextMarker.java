package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;

public class TextMarker extends AbstractMarker {
  private String text = "?";

  private Font font;

  private Measure<Length> textSize = Measure.valueOf(10, NonSI.PIXEL);

  private String textFaceName = "san-serif";

  private long lastScale;

  public TextMarker(final Map<String, Object> properties) {
    setProperties(properties);
  }

  public TextMarker(final String textFaceName, final Measure<Length> textSize, final String text) {
    this.textFaceName = textFaceName;
    this.textSize = textSize;
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

  @Override
  public int hashCode() {
    return getText().hashCode();
  }

  // @Override
  // public Icon getIcon(final MarkerStyle style) {
  // final Shape shape = getShape();
  // final AffineTransform shapeTransform = AffineTransform.getScaleInstance(16,
  // 16);
  //
  // final BufferedImage image = new BufferedImage(16, 16,
  // BufferedImage.TYPE_INT_ARGB);
  // final Graphics2D graphics = image.createGraphics();
  // final Shape newShape = new
  // GeneralPath(shape).createTransformedShape(shapeTransform);
  // if (style.setMarkerFillStyle(null, graphics)) {
  // graphics.fill(newShape);
  // }
  // if (style.setMarkerLineStyle(null, graphics)) {
  // graphics.draw(newShape);
  // }
  // graphics.dispose();
  // return new ImageIcon(image);
  // }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics, final MarkerStyle style,
    final double modelX, final double modelY, double orientation) {
    if (viewport != null) {
      final long scale = (long)viewport.getScale();
      if (this.font == null || this.lastScale != scale) {
        this.lastScale = scale;
        final double fontSize = Viewport2D.toDisplayValue(viewport, this.textSize);
        this.font = new Font(this.textFaceName, 0, (int)Math.ceil(fontSize));
      }
      final AffineTransform savedTransform = graphics.getTransform();
      try {
        final Measure<Length> markerWidth = style.getMarkerWidth();
        final double mapWidth = Viewport2D.toDisplayValue(viewport, markerWidth);
        final Measure<Length> markerHeight = style.getMarkerHeight();
        final double mapHeight = Viewport2D.toDisplayValue(viewport, markerHeight);
        final String orientationType = style.getMarkerOrientationType();
        if ("none".equals(orientationType)) {
          orientation = 0;
        }

        translateMarker(viewport, graphics, style, modelX, modelY, mapWidth, mapHeight,
          orientation);

        final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
        final GlyphVector glyphVector = this.font.createGlyphVector(fontRenderContext, this.text);
        final Shape shape = glyphVector.getOutline();

        final AffineTransform shapeTransform = AffineTransform.getScaleInstance(mapWidth,
          mapHeight);
        final Shape newShape = new GeneralPath(shape).createTransformedShape(shapeTransform);
        if (style.setMarkerFillStyle(viewport, graphics)) {
          graphics.fill(newShape);
        }
        if (style.setMarkerLineStyle(viewport, graphics)) {
          graphics.draw(newShape);
        }
      } finally {
        graphics.setTransform(savedTransform);
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

  public void setTextSize(final Measure<Length> textSize) {
    final Object oldValue = this.textSize;
    this.textSize = MarkerStyle.getWithDefault(textSize, MarkerStyle.TEN_PIXELS);
    this.font = null;
    firePropertyChange("textSize", oldValue, this.textSize);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    return map;
  }

  @Override
  public String toString() {
    return getText();
  }
}

package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import com.revolsys.awt.CloseableAffineTransform;
import com.revolsys.io.BaseCloseable;
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
    try (
      BaseCloseable transformCloseable = new CloseableAffineTransform(graphics)) {
      Viewport2D.setUseModelCoordinates(viewport, graphics, false);
      final Measure<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = Viewport2D.toDisplayValue(viewport, markerHeight);
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }
      final int fontSize = (int)mapHeight;
      if (this.font == null || this.font.getSize() != fontSize) {
        this.font = new Font(this.textFaceName, 0, fontSize);
      }

      final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
      final GlyphVector glyphVector = this.font.createGlyphVector(fontRenderContext, this.text);
      final Shape shape = glyphVector.getOutline();
      final GeneralPath newShape = new GeneralPath(shape);
      final Rectangle2D bounds = newShape.getBounds2D();
      final double shapeWidth = bounds.getWidth();
      final double shapeHeight = bounds.getHeight();

      if (viewport != null) {
        final double[] viewCoordinates = viewport.toViewCoordinates(modelX, modelY);
        graphics.translate(viewCoordinates[0], viewCoordinates[1]);
      }
      final double markerOrientation = style.getMarkerOrientation();
      orientation = -orientation + markerOrientation;
      if (orientation != 0) {
        graphics.rotate(Math.toRadians(orientation));
      }

      final Measure<Length> deltaX = style.getMarkerDx();
      final Measure<Length> deltaY = style.getMarkerDy();
      double dx = Viewport2D.toDisplayValue(viewport, deltaX);
      double dy = Viewport2D.toDisplayValue(viewport, deltaY);
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

      if (style.setMarkerFillStyle(viewport, graphics)) {
        graphics.fill(newShape);
      }
      if (style.setMarkerLineStyle(viewport, graphics)) {
        graphics.draw(newShape);
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
    addToMap(map, "type", "markerText");
    addToMap(map, "textFaceName", this.textFaceName);
    addToMap(map, "text", this.text);
    return map;
  }

  @Override
  public String toString() {
    return getText();
  }
}

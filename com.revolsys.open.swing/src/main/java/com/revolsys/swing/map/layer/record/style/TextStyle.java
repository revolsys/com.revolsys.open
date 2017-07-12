package com.revolsys.swing.map.layer.record.style;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.logging.Logs;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class TextStyle extends BaseObjectWithPropertiesAndChange
  implements MapSerializer, Cloneable {

  private static final String AUTO = "auto";

  private static final Map<String, Object> DEFAULT_VALUES = new TreeMap<>();

  private static final Set<String> PROPERTY_NAMES = new HashSet<>();

  static {
    // addProperty("text-allow-overlap",DataTypes.);
    // addProperty("text-avoid-edges",DataTypes.);
    addStyleProperty("textBoxColor", WebColors.Gainsboro);
    addStyleProperty("textBoxOpacity", 255);
    // addProperty("text-character-spacing",DataTypes.);
    // addProperty("text-clip",DataTypes.);
    // addProperty("text-comp-op",DataTypes.);
    addStyleProperty("textDx", MarkerStyle.ZERO_PIXEL);
    addStyleProperty("textDy", MarkerStyle.ZERO_PIXEL);
    addStyleProperty("textFaceName", "Arial");
    addStyleProperty("textFill", WebColors.Black);
    addStyleProperty("textHaloFill", WebColors.White);
    addStyleProperty("textHaloRadius", 0);
    addStyleProperty("textHorizontalAlignment", AUTO);
    // addProperty("text-label-position-tolerance",DataTypes.);
    // addProperty("text-line-spacing",DataTypes.);
    // addProperty("text-max-char-angle-delta",DataTypes.);
    // addProperty("text-min-distance",DataTypes.);
    // addProperty("text-min-padding",DataTypes.);
    // addProperty("text-min-path-length",DataTypes.);
    addStyleProperty("textName", "");
    addStyleProperty("textOpacity", 255);
    addStyleProperty("textOrientation", 0.0);
    addStyleProperty("textOrientationType", AUTO);
    // addProperty("text-placement",DataTypes.);
    addStyleProperty("textPlacementType", AUTO);
    // addProperty("text-placements",DataTypes.);
    // addProperty("text-ratio",DataTypes.);
    addStyleProperty("textSize", MarkerStyle.TEN_PIXELS);
    // addProperty("text-spacing",DataTypes.);
    // addProperty("text-transform",DataTypes.);
    addStyleProperty("textVerticalAlignment", AUTO);
    // addProperty("text-wrap-before",DataTypes.);
    // addProperty("text-wrap-character",DataTypes.);
    // addProperty("text-wrap-width", Double.class);
  }

  private static final void addStyleProperty(final String name, final Object defaultValue) {
    PROPERTY_NAMES.add(name);
    DEFAULT_VALUES.put(name, defaultValue);
  }

  public static TextStyle text() {
    return new TextStyle();
  }

  private Font font;

  private long lastScale = 0;

  private Color textBoxColor = WebColors.Gainsboro;

  private int textBoxOpacity = 255;

  private Measure<Length> textDx = GeometryStyle.ZERO_PIXEL;

  private Measure<Length> textDy = GeometryStyle.ZERO_PIXEL;

  private String textFaceName = "Arial";

  private Color textFill = WebColors.Black;

  private Color textHaloFill = WebColors.White;

  private double textHaloRadius = 0;

  private String textHorizontalAlignment = AUTO;

  private String textName = "";

  private int textOpacity = 255;

  /** The orientation of the text in a clockwise direction from the east axis. */
  private double textOrientation = 0;

  private String textOrientationType = AUTO;

  private String textPlacementType = AUTO;

  private Measure<Length> textSize = GeometryStyle.TEN_PIXELS;

  private String textVerticalAlignment = AUTO;

  public TextStyle() {
  }

  public TextStyle(final Map<String, Object> style) {
    setProperties(style);
  }

  @Override
  public TextStyle clone() {
    return (TextStyle)super.clone();
  }

  public void drawTextIcon(final Graphics2D graphics, final int size) {
    double orientation = getTextOrientation();

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    final String textFaceName = getTextFaceName();
    final Font font = new Font(textFaceName, 0, size);
    graphics.setFont(font);
    final FontMetrics fontMetrics = graphics.getFontMetrics();

    int min;
    int max;
    int boxWidth;
    if (size == 12) {
      min = 0;
      max = 15;
      boxWidth = 16;
    } else {
      min = 2;
      max = 12;
      boxWidth = 12;
    }
    final String text = "A";
    final Rectangle2D bounds = fontMetrics.getStringBounds(text, graphics);
    final double width = bounds.getWidth();
    final double height = fontMetrics.getAscent();
    final String horizontalAlignment = getTextHorizontalAlignment();
    final int x;
    if ("right".equals(horizontalAlignment)) {
      x = max - (int)Math.round(width);
    } else if ("center".equals(horizontalAlignment) || "auto".equals(horizontalAlignment)) {
      x = 8 - (int)Math.round(width / 2);
    } else {
      x = min;
    }
    final String verticalAlignment = getTextVerticalAlignment();
    final int y;
    if ("top".equals(verticalAlignment)) {
      y = (int)height + min - 1;
    } else if ("middle".equals(verticalAlignment) || "auto".equals(verticalAlignment)) {
      y = 7 + (int)Math.round(height / 2);
    } else {
      y = 16 - min;
    }
    if (orientation != 0) {
      if (orientation > 270) {
        orientation -= 360;
      }
      graphics.rotate(-Math.toRadians(orientation), 8, 8);
    }

    final int textBoxOpacity = getTextBoxOpacity();
    final Color textBoxColor = getTextBoxColor();
    if (textBoxOpacity > 0 && textBoxColor != null) {
      graphics.setPaint(textBoxColor);
      final RoundRectangle2D.Double box = new RoundRectangle2D.Double(min, min, boxWidth, boxWidth,
        5, 5);
      graphics.fill(box);
    }

    final double textHaloRadius = getTextHaloRadius();
    if (textHaloRadius > 0) {
      final Stroke savedStroke = graphics.getStroke();
      final Stroke outlineStroke = new BasicStroke((float)(textHaloRadius + 1),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
      graphics.setColor(getTextHaloFill());
      graphics.setStroke(outlineStroke);
      final FontRenderContext fontRenderContext = graphics.getFontRenderContext();
      final TextLayout textLayout = new TextLayout(text, font, fontRenderContext);
      final Shape outlineShape = textLayout.getOutline(TextStyleRenderer.NOOP_TRANSFORM);
      graphics.draw(outlineShape);
      graphics.setStroke(savedStroke);
    }

    graphics.setColor(getTextFill());
    if (textBoxOpacity > 0 && textBoxOpacity < 255) {
      graphics.setComposite(AlphaComposite.SrcOut);
      graphics.drawString(text, x, y);
      graphics.setComposite(AlphaComposite.DstOver);
      graphics.drawString(text, x, y);
    } else {
      graphics.setComposite(AlphaComposite.SrcOver);
      graphics.drawString(text, x, y);
    }
  }

  public Font getFont(final Viewport2D viewport) {
    final int style = 0;
    // if (textStyle.getFontWeight() == FontWeight.BOLD) {
    // style += Font.BOLD;
    // }
    // if (textStyle.getFontStyle() == FontStyle.ITALIC) {
    // style += Font.ITALIC;
    // }
    final double fontSize = Viewport2D.toDisplayValue(viewport, this.textSize);
    return new Font(this.textFaceName, style, (int)Math.ceil(fontSize));
  }

  public Color getTextBoxColor() {
    return this.textBoxColor;
  }

  public int getTextBoxOpacity() {
    return this.textBoxOpacity;
  }

  public Measure<Length> getTextDx() {
    return this.textDx;
  }

  public Measure<Length> getTextDy() {
    return this.textDy;
  }

  public String getTextFaceName() {
    return this.textFaceName;
  }

  public Color getTextFill() {
    return this.textFill;
  }

  public Color getTextHaloFill() {
    return this.textHaloFill;
  }

  public double getTextHaloRadius() {
    return this.textHaloRadius;
  }

  public String getTextHorizontalAlignment() {
    return this.textHorizontalAlignment;
  }

  public String getTextName() {
    return this.textName;
  }

  public int getTextOpacity() {
    return this.textOpacity;
  }

  public double getTextOrientation() {
    return this.textOrientation;
  }

  public String getTextOrientationType() {
    return this.textOrientationType;
  }

  public String getTextPlacementType() {
    return this.textPlacementType;
  }

  public Measure<Length> getTextSize() {
    return this.textSize;
  }

  public Unit<Length> getTextSizeUnit() {
    return this.textSize.getUnit();
  }

  public String getTextVerticalAlignment() {
    return this.textVerticalAlignment;
  }

  @Override
  public void setPropertyError(final String name, final Object value, final Throwable e) {
    Logs.error(this, "Error setting " + name + '=' + value, e);
  }

  public void setTextBoxColor(final Color textBoxColor) {
    final Object oldTextBoxColor = this.textBoxColor;
    final Object oldTextBoxOpacity = this.textBoxOpacity;
    if (textBoxColor == null) {
      this.textBoxColor = null;
      this.textBoxOpacity = 255;
    } else {
      this.textBoxColor = textBoxColor;
      this.textBoxOpacity = textBoxColor.getAlpha();
    }
    firePropertyChange("textBoxColor", oldTextBoxColor, this.textBoxColor);
    firePropertyChange("textBoxOpacity", oldTextBoxOpacity, this.textBoxOpacity);
  }

  public void setTextBoxOpacity(final int textBoxOpacity) {
    final Object oldTextBoxColor = this.textBoxColor;
    final Object oldTextBoxOpacity = this.textBoxOpacity;
    if (textBoxOpacity < 0 || textBoxOpacity > 255) {
      throw new IllegalArgumentException("Text box opacity must be between 0 - 255");
    } else {
      this.textBoxOpacity = textBoxOpacity;
      this.textBoxColor = WebColors.newAlpha(this.textBoxColor, this.textBoxOpacity);
    }
    firePropertyChange("textBoxColor", oldTextBoxColor, this.textBoxColor);
    firePropertyChange("textBoxOpacity", oldTextBoxOpacity, this.textBoxOpacity);
  }

  public void setTextDx(final Measure<Length> textDx) {
    final Object oldValue = this.textDy;
    if (textDx == null) {
      this.textDx = this.textDy;
    } else {
      this.textDx = textDx;
    }
    firePropertyChange("textDx", oldValue, this.textDx);
    updateTextDeltaUnits(this.textDx.getUnit());
  }

  public void setTextDy(final Measure<Length> textDy) {
    final Object oldValue = this.textDy;
    if (textDy == null) {
      this.textDy = this.textDx;
    } else {
      this.textDy = textDy;
    }
    firePropertyChange("textDy", oldValue, this.textDy);
    updateTextDeltaUnits(this.textDy.getUnit());
  }

  public void setTextFaceName(final String textFaceName) {
    final Object oldValue = this.textFaceName;
    this.textFaceName = textFaceName;
    this.font = null;
    firePropertyChange("textFaceName", oldValue, this.textFaceName);
  }

  public void setTextFill(final Color fill) {
    final Object oldTextFill = this.textFill;
    final Object oldTextOpacity = this.textOpacity;
    if (fill == null) {
      this.textFill = new Color(0, 0, 0, this.textOpacity);
    } else {
      this.textFill = fill;
      this.textOpacity = fill.getAlpha();
    }
    firePropertyChange("textFill", oldTextFill, this.textFill);
    firePropertyChange("textOpacity", oldTextOpacity, this.textOpacity);
  }

  public void setTextHaloFill(final Color fill) {
    final Object oldValue = this.textHaloFill;
    if (fill == null) {
      this.textHaloFill = new Color(0, 0, 0, this.textOpacity);
    } else {
      this.textHaloFill = WebColors.newAlpha(fill, this.textOpacity);
    }
    firePropertyChange("textHaloFill", oldValue, this.textHaloFill);
  }

  public void setTextHaloRadius(final double textHaloRadius) {
    final Object oldValue = this.textHaloRadius;
    this.textHaloRadius = textHaloRadius;
    firePropertyChange("textHaloRadius", oldValue, this.textHaloRadius);
  }

  public void setTextHaloRadius(final Measure<Length> textHaloRadius) {
    setTextHaloRadius(textHaloRadius.doubleValue(textHaloRadius.getUnit()));
  }

  public void setTextHorizontalAlignment(final String textHorizontalAlignment) {
    final Object oldValue = this.textHorizontalAlignment;
    if (Property.hasValue(textHorizontalAlignment)) {
      this.textHorizontalAlignment = textHorizontalAlignment;
    } else {
      this.textHorizontalAlignment = AUTO;
    }
    firePropertyChange("textHorizontalAlignment", oldValue, this.textHorizontalAlignment);
  }

  public void setTextName(final String textName) {
    final Object oldValue = this.textName;
    if (textName == null) {
      this.textName = "";
    } else {
      this.textName = textName;
    }
    firePropertyChange("textName", oldValue, this.textName);
  }

  public void setTextOpacity(final int textOpacity) {
    final Object oldTextFill = this.textFill;
    final Object oldTextOpacity = this.textOpacity;
    final Object oldTextHaloFill = this.textHaloFill;
    if (textOpacity < 0 || textOpacity > 255) {
      throw new IllegalArgumentException("Text opacity must be between 0 - 255");
    } else {
      this.textOpacity = textOpacity;
      this.textFill = WebColors.newAlpha(this.textFill, this.textOpacity);
      this.textHaloFill = WebColors.newAlpha(this.textHaloFill, this.textOpacity);
    }
    firePropertyChange("textFill", oldTextFill, this.textFill);
    firePropertyChange("textOpacity", oldTextOpacity, this.textOpacity);
    firePropertyChange("textHaloFill", oldTextHaloFill, this.textHaloFill);
  }

  public void setTextOrientation(final double textOrientation) {
    final Object oldValue = this.textOrientation;
    this.textOrientation = textOrientation;
    firePropertyChange("textOrientation", oldValue, this.textOrientation);
  }

  public void setTextOrientationType(final String textOrientationType) {
    final Object oldValue = this.textOrientationType;
    this.textOrientationType = textOrientationType;
    firePropertyChange("textOrientationType", oldValue, this.textOrientationType);
  }

  @Deprecated
  public void setTextPlacement(final String textPlacementType) {
    setTextPlacementType(textPlacementType);
  }

  public void setTextPlacementType(String textPlacementType) {
    final Object oldValue = this.textPlacementType;
    if (Property.hasValue(textPlacementType)) {
      textPlacementType = Strings.replaceAll(textPlacementType, "^point\\(", "vertex\\(");
      this.textPlacementType = textPlacementType;
    } else {
      this.textPlacementType = AUTO;
    }
    firePropertyChange("textPlacementType", oldValue, this.textPlacementType);
  }

  public void setTextSize(final Measure<Length> textSize) {
    final Object oldValue = this.textSize;
    this.textSize = MarkerStyle.getWithDefault(textSize, MarkerStyle.TEN_PIXELS);
    this.font = null;
    firePropertyChange("textSize", oldValue, this.textSize);
  }

  public synchronized void setTextStyle(final Viewport2D viewport, final Graphics2D graphics) {
    if (viewport == null) {
      final Font font = new Font(this.textFaceName, 0, this.textSize.getValue().intValue());
      graphics.setFont(font);
    } else {
      final long scale = (long)viewport.getScale();
      if (this.font == null || this.lastScale != scale) {
        this.lastScale = scale;
        final int style = 0;
        // if (textStyle.getFontWeight() == FontWeight.BOLD) {
        // style += Font.BOLD;
        // }
        // if (textStyle.getFontStyle() == FontStyle.ITALIC) {
        // style += Font.ITALIC;
        // }
        final double fontSize = Viewport2D.toDisplayValue(viewport, this.textSize);
        this.font = new Font(this.textFaceName, style, (int)Math.ceil(fontSize));
      }
      graphics.setFont(this.font);
    }
  }

  public void setTextVerticalAlignment(final String textVerticalAlignment) {
    final Object oldValue = this.textVerticalAlignment;
    if (Property.hasValue(textVerticalAlignment)) {
      this.textVerticalAlignment = textVerticalAlignment;
    } else {
      this.textVerticalAlignment = AUTO;
    }
    firePropertyChange("textVerticalAlignment", oldValue, this.textVerticalAlignment);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    for (final String name : PROPERTY_NAMES) {
      Object value = Property.get(this, name);
      if (value instanceof Color) {
        final Color color = (Color)value;
        value = WebColors.newAlpha(color, 255);
      }
      boolean defaultEqual = false;
      if (DEFAULT_VALUES.containsKey(name)) {
        final Object defaultValue = DEFAULT_VALUES.get(name);
        defaultEqual = DataType.equal(defaultValue, value);
      }
      if (!defaultEqual) {

        addToMap(map, name, value);
      }
    }
    return map;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }

  private void updateTextDeltaUnits(final Unit<Length> unit) {
    if (!this.textDx.getUnit().equals(unit)) {
      final double oldValue = this.textDx.getValue().doubleValue();
      final Measure<Length> newValue = Measure.valueOf(oldValue, unit);
      setTextDx(newValue);
    }
    if (!this.textDy.getUnit().equals(unit)) {
      final double oldValue = this.textDy.getValue().doubleValue();
      final Measure<Length> newValue = Measure.valueOf(oldValue, unit);
      setTextDy(newValue);
    }
  }
}

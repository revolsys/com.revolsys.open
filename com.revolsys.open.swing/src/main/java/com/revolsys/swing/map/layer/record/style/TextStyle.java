package com.revolsys.swing.map.layer.record.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.awt.WebColors;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.equals.Equals;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.Exceptions;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class TextStyle implements MapSerializer, Cloneable {

  private static final String AUTO = "auto";

  private static final Map<String, Object> DEFAULT_VALUES = new TreeMap<>();

  private static final Map<String, DataType> PROPERTIES = new TreeMap<>();

  static {
    // addProperty("text-allow-overlap",DataTypes.);
    // addProperty("text-avoid-edges",DataTypes.);
    addProperty("textBoxColor", DataTypes.COLOR, WebColors.Gainsboro);
    addProperty("textBoxOpacity", DataTypes.INT, 255);
    // addProperty("text-character-spacing",DataTypes.);
    // addProperty("text-clip",DataTypes.);
    // addProperty("text-comp-op",DataTypes.);
    addProperty("textDx", DataTypes.MEASURE, MarkerStyle.ZERO_PIXEL);
    addProperty("textDy", DataTypes.MEASURE, MarkerStyle.ZERO_PIXEL);
    addProperty("textFaceName", DataTypes.STRING, "Arial");
    addProperty("textFill", DataTypes.COLOR, WebColors.Black);
    addProperty("textHaloFill", DataTypes.COLOR, WebColors.White);
    addProperty("textHaloRadius", DataTypes.DOUBLE, 0);
    addProperty("textHorizontalAlignment", DataTypes.STRING, AUTO);
    // addProperty("text-label-position-tolerance",DataTypes.);
    // addProperty("text-line-spacing",DataTypes.);
    // addProperty("text-max-char-angle-delta",DataTypes.);
    // addProperty("text-min-distance",DataTypes.);
    // addProperty("text-min-padding",DataTypes.);
    // addProperty("text-min-path-length",DataTypes.);
    addProperty("textName", DataTypes.STRING, "");
    addProperty("textOpacity", DataTypes.INT, 255);
    addProperty("textOrientation", DataTypes.DOUBLE, 0.0);
    addProperty("textOrientationType", DataTypes.STRING, AUTO);
    // addProperty("text-placement",DataTypes.);
    addProperty("textPlacementType", DataTypes.STRING, AUTO);
    // addProperty("text-placements",DataTypes.);
    // addProperty("text-ratio",DataTypes.);
    addProperty("textSize", DataTypes.MEASURE, MarkerStyle.TEN_PIXELS);
    // addProperty("text-spacing",DataTypes.);
    // addProperty("text-transform",DataTypes.);
    addProperty("textVerticalAlignment", DataTypes.STRING, AUTO);
    // addProperty("text-wrap-before",DataTypes.);
    // addProperty("text-wrap-character",DataTypes.);
    // addProperty("text-wrap-width", Double.class);
  }

  private static final void addProperty(final String name, final DataType dataType,
    final Object defaultValue) {
    PROPERTIES.put(name, dataType);
    DEFAULT_VALUES.put(name, defaultValue);
  }

  private static Object getValue(final String propertyName, final Object value) {
    final DataType dataType = PROPERTIES.get(propertyName);
    if (dataType == null) {
      return null;
    } else {
      return dataType.toObject(value);
    }
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
    for (final Entry<String, Object> entry : style.entrySet()) {
      final String propertyName = entry.getKey();
      if (PROPERTIES.containsKey(propertyName)) {
        Object value = entry.getValue();
        if (propertyName.equals("textHaloRadius")) {
          String string = value.toString();
          string = string.replaceAll(" \\[.*\\]", "");
          value = string;

        }
        final Object propertyValue = getValue(propertyName, value);
        try {
          JavaBeanUtil.setProperty(this, propertyName, propertyValue);
        } catch (final Throwable e) {
          Exceptions.log(getClass(), "Unable to set style " + propertyName + "=" + propertyValue,
            e);
        }
      }
    }
  }

  @Override
  public TextStyle clone() {
    try {
      return (TextStyle)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
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

  public void setTextBoxColor(final Color textBoxColor) {
    if (textBoxColor == null) {
      this.textBoxColor = null;
      this.textBoxOpacity = 255;
    } else {
      this.textBoxColor = textBoxColor;
      this.textBoxOpacity = textBoxColor.getAlpha();
    }
  }

  public void setTextBoxOpacity(final int textBoxOpacity) {
    if (textBoxOpacity < 0 || textBoxOpacity > 255) {
      throw new IllegalArgumentException("Text box opacity must be between 0 - 255");
    } else {
      this.textBoxOpacity = textBoxOpacity;
      this.textBoxColor = WebColors.setAlpha(this.textBoxColor, this.textBoxOpacity);
    }
  }

  public void setTextDx(final Measure<Length> textDx) {
    this.textDx = MarkerStyle.getWithDefault(textDx, MarkerStyle.ZERO_PIXEL);
  }

  public void setTextDy(final Measure<Length> textDy) {
    this.textDy = MarkerStyle.getWithDefault(textDy, MarkerStyle.ZERO_PIXEL);
  }

  public void setTextFaceName(final String textFaceName) {
    this.textFaceName = textFaceName;
    this.font = null;
  }

  public void setTextFill(final Color fill) {
    if (fill == null) {
      this.textFill = new Color(0, 0, 0, this.textOpacity);
    } else {
      this.textFill = fill;
      this.textOpacity = fill.getAlpha();
    }
  }

  public void setTextHaloFill(final Color fill) {
    if (fill == null) {
      this.textHaloFill = new Color(0, 0, 0, this.textOpacity);
    } else {
      this.textHaloFill = WebColors.setAlpha(fill, this.textOpacity);
    }
  }

  public void setTextHaloRadius(final double textHaloRadius) {
    this.textHaloRadius = textHaloRadius;
  }

  public void setTextHaloRadius(final Measure<Length> textHaloRadius) {
    setTextHaloRadius(textHaloRadius.doubleValue(textHaloRadius.getUnit()));
  }

  public void setTextHorizontalAlignment(final String textHorizontalAlignment) {
    if (Property.hasValue(textHorizontalAlignment)) {
      this.textHorizontalAlignment = textHorizontalAlignment;
    } else {
      this.textHorizontalAlignment = AUTO;
    }
  }

  public void setTextName(final String textName) {
    if (textName == null) {
      this.textName = "";
    } else {
      this.textName = textName;
    }
  }

  public void setTextOpacity(final int textOpacity) {
    if (textOpacity < 0 || textOpacity > 255) {
      throw new IllegalArgumentException("Text opacity must be between 0 - 255");
    } else {
      this.textOpacity = textOpacity;
      this.textFill = WebColors.setAlpha(this.textFill, this.textOpacity);
      this.textHaloFill = WebColors.setAlpha(this.textHaloFill, this.textOpacity);
    }
  }

  public void setTextOrientation(final double textOrientation) {
    this.textOrientation = textOrientation;
  }

  public void setTextOrientationType(final String textOrientationType) {
    this.textOrientationType = textOrientationType;
  }

  public void setTextPlacement(final String textPlacementType) {
    setTextPlacementType(textPlacementType);
  }

  public void setTextPlacementType(final String textPlacementType) {
    if (Property.hasValue(textPlacementType)) {
      this.textPlacementType = textPlacementType;
    } else {
      this.textPlacementType = AUTO;
    }
  }

  public void setTextSize(final Measure<Length> textSize) {
    this.textSize = MarkerStyle.getWithDefault(textSize, MarkerStyle.TEN_PIXELS);
    this.font = null;
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
    if (Property.hasValue(textVerticalAlignment)) {
      this.textVerticalAlignment = textVerticalAlignment;
    } else {
      this.textVerticalAlignment = AUTO;
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    for (final String name : PROPERTIES.keySet()) {
      Object value = Property.get(this, name);
      if (value instanceof Color) {
        final Color color = (Color)value;
        value = WebColors.setAlpha(color, 255);
      }
      boolean defaultEqual = false;
      if (DEFAULT_VALUES.containsKey(name)) {
        Object defaultValue = DEFAULT_VALUES.get(name);
        defaultValue = getValue(name, defaultValue);
        defaultEqual = Equals.equal(defaultValue, value);
      }
      if (!defaultEqual) {

        MapSerializerUtil.add(map, name, value);
      }
    }
    return map;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }
}

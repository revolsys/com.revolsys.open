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
import com.revolsys.beans.AbstractPropertyChangeSupportProxy;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.Exceptions;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class TextStyle extends AbstractPropertyChangeSupportProxy
  implements MapSerializer, Cloneable {

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
    return (TextStyle)super.clone();
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
      this.textBoxColor = WebColors.setAlpha(this.textBoxColor, this.textBoxOpacity);
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
      this.textHaloFill = WebColors.setAlpha(fill, this.textOpacity);
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
      this.textFill = WebColors.setAlpha(this.textFill, this.textOpacity);
      this.textHaloFill = WebColors.setAlpha(this.textHaloFill, this.textOpacity);
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

  public void setTextPlacement(final String textPlacementType) {
    setTextPlacementType(textPlacementType);
  }

  public void setTextPlacementType(final String textPlacementType) {
    final Object oldValue = this.textPlacementType;
    if (Property.hasValue(textPlacementType)) {
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
        defaultEqual = DataType.equal(defaultValue, value);
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

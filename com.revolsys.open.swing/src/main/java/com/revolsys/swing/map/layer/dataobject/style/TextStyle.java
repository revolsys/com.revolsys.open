package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.JavaBeanUtil;

public class TextStyle implements MapSerializer {

  private static final Map<String, Object> DEFAULT_VALUES = new TreeMap<String, Object>();

  private static final Map<String, Class<?>> PROPERTIES = new TreeMap<String, Class<?>>();

  static {
    // addProperty("text-allow-overlap",DataTypes.);
    // addProperty("text-avoid-edges",DataTypes.);
    addProperty("textBoxColor", Color.class, new Color(223, 223, 233, 127));
    // addProperty("text-character-spacing",DataTypes.);
    // addProperty("text-clip",DataTypes.);
    // addProperty("text-comp-op",DataTypes.);
    addProperty("textDx", Measure.class, GeometryStyle.ZERO_PIXEL);
    addProperty("textDy", Measure.class, GeometryStyle.ZERO_PIXEL);
    addProperty("textFaceName", String.class, "Arial");
    addProperty("textFill", Color.class, new Color(0, 0, 0));
    addProperty("textHaloFill", Color.class, new Color(255, 255, 255));
    addProperty("textHaloRadius", Double.class, GeometryStyle.ZERO_PIXEL);
    addProperty("textHorizontalAlignment", String.class, "auto");
    // addProperty("text-label-position-tolerance",DataTypes.);
    // addProperty("text-line-spacing",DataTypes.);
    // addProperty("text-max-char-angle-delta",DataTypes.);
    // addProperty("text-min-distance",DataTypes.);
    // addProperty("text-min-padding",DataTypes.);
    // addProperty("text-min-path-length",DataTypes.);
    addProperty("textName", String.class, "");
    addProperty("textOpacity", Integer.class, 255);
    addProperty("textOrientation", Double.class, 0.0);
    addProperty("textOrientationType", String.class, "auto");
    // addProperty("text-placement",DataTypes.);
    addProperty("textPlacementType", String.class, "dummy");
    // addProperty("text-placements",DataTypes.);
    // addProperty("text-ratio",DataTypes.);
    addProperty("textSize", Measure.class, GeometryStyle.TEN_PIXELS);
    // addProperty("text-spacing",DataTypes.);
    // addProperty("text-transform",DataTypes.);
    addProperty("textVerticalAlignment", String.class, "auto");
    // addProperty("text-wrap-before",DataTypes.);
    // addProperty("text-wrap-character",DataTypes.);
    // addProperty("text-wrap-width", Double.class);
  }

  private static final void addProperty(final String name,
    final Class<?> dataClass, final Object defaultValue) {
    PROPERTIES.put(name, dataClass);
    DEFAULT_VALUES.put(name, defaultValue);
  }

  private static Object getValue(final String propertyName, final Object value) {
    final Class<?> dataClass = PROPERTIES.get(propertyName);
    if (dataClass == null) {
      return null;
    } else {
      return StringConverterRegistry.toObject(dataClass, value);
    }
  }

  public static TextStyle text() {
    return new TextStyle();
  }

  private Font font;

  private long lastScale = 0;

  private Color textBoxColor = new Color(223, 223, 233, 127);

  private Measure<Length> textDx = GeometryStyle.ZERO_PIXEL;

  private Measure<Length> textDy = GeometryStyle.ZERO_PIXEL;

  private String textFaceName = "Arial";

  private Color textFill = new Color(0, 0, 0);

  private Color textHaloFill = new Color(255, 255, 255);

  private Measure<Length> textHaloRadius = GeometryStyle.ZERO_PIXEL;

  private String textHorizontalAlignment = "auto";

  private String textName = "";

  private int textOpacity = 255;

  /** The orientation of the text in a clockwise direction from the east axis. */
  private double textOrientation = 0;

  private String textOrientationType = "auto";

  private String textPlacementType = "dummy";

  private Measure<Length> textSizeMeasure = GeometryStyle.TEN_PIXELS;

  private String textVerticalAlignment = "auto";

  public TextStyle() {
  }

  public TextStyle(final Map<String, Object> style) {
    for (final Entry<String, Object> entry : style.entrySet()) {
      final String propertyName = entry.getKey();
      final Object value = entry.getValue();
      final Object propertyValue = getValue(propertyName, value);
      if (propertyValue != null) {
        JavaBeanUtil.setProperty(this, propertyName, propertyValue);
      }
    }
  }

  public Color getTextBoxColor() {
    return textBoxColor;
  }

  public Measure<Length> getTextDx() {
    return textDx;
  }

  public Measure<Length> getTextDy() {
    return textDy;
  }

  public String getTextFaceName() {
    return textFaceName;
  }

  public Color getTextFill() {
    return textFill;
  }

  public Color getTextHaloFill() {
    return textHaloFill;
  }

  public Measure<Length> getTextHaloRadius() {
    return textHaloRadius;
  }

  public String getTextHorizontalAlignment() {
    return textHorizontalAlignment;
  }

  public String getTextName() {
    return textName;
  }

  public int getTextOpacity() {
    return textOpacity;
  }

  public double getTextOrientation() {
    return textOrientation;
  }

  public String getTextOrientationType() {
    return textOrientationType;
  }

  public String getTextPlacementType() {
    return textPlacementType;
  }

  public Measure<Length> getTextSize() {
    return textSizeMeasure;
  }

  public String getTextVerticalAlignment() {
    return textVerticalAlignment;
  }

  public void setTextBoxColor(final Color textBoxColor) {
    this.textBoxColor = textBoxColor;
  }

  public void setTextDx(final Measure<Length> textDx) {
    this.textDx = MarkerStyle.getWithDefault(textDx, MarkerStyle.ZERO_PIXEL);
  }

  public void setTextDy(final Measure<Length> textDy) {
    this.textDy = MarkerStyle.getWithDefault(textDy, MarkerStyle.ZERO_PIXEL);
  }

  public void setTextFaceName(final String textFaceName) {
    this.textFaceName = textFaceName;
  }

  public void setTextFill(final Color fill) {
    if (fill == null) {
      this.textFill = new Color(0, 0, 0, textOpacity);
    } else {
      this.textFill = fill;
      this.textOpacity = fill.getAlpha();
    }
  }

  public void setTextHaloFill(final Color fill) {
    if (fill == null) {
      this.textHaloFill = new Color(0, 0, 0, textOpacity);
    } else {
      this.textHaloFill = fill;
    }
  }

  public void setTextHaloRadius(final Measure<Length> textHaloRadius) {
    this.textHaloRadius = textHaloRadius;
  }

  public void setTextHorizontalAlignment(final String textHorizontalAlignment) {
    if (StringUtils.hasText(textHorizontalAlignment)) {
      this.textHorizontalAlignment = textHorizontalAlignment;
    } else {
      this.textHorizontalAlignment = "auto";
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
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.textOpacity = textOpacity;
      this.textFill = MarkerStyle.getColorWithOpacity(textFill,
        this.textOpacity);
      this.textHaloFill = MarkerStyle.getColorWithOpacity(textHaloFill,
        this.textOpacity);
    }
  }

  public void setTextOrientation(final double textOrientation) {
    this.textOrientation = textOrientation;
  }

  public void setTextOrientationType(final String textOrientationType) {
    this.textOrientationType = textOrientationType;
  }

  public void setTextPlacementType(final String textPlacementType) {
    if (StringUtils.hasText(textPlacementType)) {
      this.textPlacementType = textPlacementType;
    } else {
      this.textPlacementType = "dummy";
    }
  }

  public void setTextSize(final Measure<Length> textSize) {
    this.textSizeMeasure = MarkerStyle.getWithDefault(textSize,
      MarkerStyle.TEN_PIXELS);
  }

  public synchronized void setTextStyle(final Viewport2D viewport,
    final Graphics2D graphics) {
    final long scale = (long)viewport.getScale();
    if (font == null || lastScale != scale) {
      lastScale = scale;
      final int style = 0;
      // if (textStyle.getFontWeight() == FontWeight.BOLD) {
      // style += Font.BOLD;
      // }
      // if (textStyle.getFontStyle() == FontStyle.ITALIC) {
      // style += Font.ITALIC;
      // }
      final double fontSize = Viewport2D.toDisplayValue(viewport,
        textSizeMeasure);
      font = new Font(textFaceName, style, (int)Math.ceil(fontSize));
    }
    graphics.setFont(font);
  }

  public void setTextVerticalAlignment(final String textVerticalAlignment) {
    if (StringUtils.hasText(textVerticalAlignment)) {
      this.textVerticalAlignment = textVerticalAlignment;
    } else {
      this.textVerticalAlignment = "auto";
    }
  }

  @Override
  public Map<String, Object> toMap() {
    return toMap(Collections.<String, Object> emptyMap());
  }

  public Map<String, Object> toMap(final Map<String, Object> defaults) {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    for (final String name : PROPERTIES.keySet()) {
      final Object value = JavaBeanUtil.getValue(this, name);

      Object defaultValue = defaults.get(name);
      if (defaultValue != null) {
        defaultValue = getValue(name, defaultValue);
      }
      if (defaultValue == null) {
        defaultValue = DEFAULT_VALUES.get(name);
      }
      if (!EqualsRegistry.equal(defaultValue, value)) {
        MapSerializerUtil.add(map, name, value);
      }
    }
    return map;
  }
}

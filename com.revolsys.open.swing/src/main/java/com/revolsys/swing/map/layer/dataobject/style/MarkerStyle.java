package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import org.springframework.core.io.Resource;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.style.marker.AbstractMarker;
import com.revolsys.swing.map.layer.dataobject.style.marker.ImageMarker;
import com.revolsys.swing.map.layer.dataobject.style.marker.Marker;
import com.revolsys.swing.map.layer.dataobject.style.marker.ShapeMarker;
import com.revolsys.util.JavaBeanUtil;

public class MarkerStyle implements Cloneable, MapSerializer {

  private static final Map<String, Object> DEFAULT_VALUES = new TreeMap<String, Object>();

  public static final AbstractMarker ELLIPSE = new ShapeMarker("ellipse");

  public static final Measure<Length> ONE_PIXEL = Measure.valueOf(1,
    NonSI.PIXEL);

  private static final Map<String, Class<?>> PROPERTIES = new TreeMap<String, Class<?>>();

  public static final Measure<Length> TEN_PIXELS = Measure.valueOf(10,
    NonSI.PIXEL);

  public static final Measure<Length> ZERO_PIXEL = Measure.valueOf(0,
    NonSI.PIXEL);

  static {
    addProperty("markerFile", String.class, null);
    addProperty("markerOpacity", Integer.class, 255);
    addProperty("markerFillOpacity", Integer.class, 255);
    addProperty("markerLineColor", Color.class, new Color(255, 255, 255, 255));
    addProperty("markerLineWidth", Measure.class, ONE_PIXEL);
    addProperty("markerLineOpacity", Double.class, 255);
    addProperty("markerPlacement", String.class, "point");
    addProperty("markerType", String.class, "ellipse");
    addProperty("markerWidth", Measure.class, TEN_PIXELS);
    addProperty("markerHeight", Measure.class, TEN_PIXELS);
    addProperty("markerFill", Color.class, new Color(0, 0, 255, 255));
    addProperty("markerAllowOverlap", Boolean.class, false);
    addProperty("markerIgnorePlacement", String.class, null);
    /*
     * addProperty("markerSpacing",DataTypes.String);
     * addProperty("markerMaxError",DataTypes.String);
     */
    addProperty("markerTransform", String.class, null);
    addProperty("markerClip", Boolean.class, true);
    addProperty("markerSmooth", Double.class, 0.0);
    addProperty("markerCompOp", String.class, null);
    addProperty("markerOrientationType", String.class, "none");
    addProperty("markerHorizontalAlignment", String.class, "auto");
    addProperty("markerVerticalAlignment", String.class, "auto");
    addProperty("markerDx", Measure.class, ZERO_PIXEL);
    addProperty("markerDy", Measure.class, ZERO_PIXEL);
  }

  private static final void addProperty(final String name,
    final Class<?> dataClass, final Object defaultValue) {
    PROPERTIES.put(name, dataClass);
    DEFAULT_VALUES.put(name, defaultValue);
  }

  public static Color getColorWithOpacity(final Color color, final int opacity) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
  }

  private static Object getValue(final String propertyName, final Object value) {
    final Class<?> dataClass = PROPERTIES.get(propertyName);
    if (dataClass == null) {
      return null;
    } else {
      return StringConverterRegistry.toObject(dataClass, value);
    }
  }

  public static <T> T getWithDefault(final T value, final T defaultValue) {
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static MarkerStyle marker(final AbstractMarker marker,
    final double markerSize, final Color lineColor, final Color fillColor) {
    final MarkerStyle style = new MarkerStyle();
    setMarker(style, marker, markerSize, lineColor, fillColor);
    return style;
  }

  public static MarkerStyle marker(final Shape shape, final double markerSize,
    final Color lineColor, final double lineWidth, final Color fillColor) {
    final AbstractMarker marker = new ShapeMarker(shape);
    return marker(marker, markerSize, lineColor, fillColor);
  }

  public static MarkerStyle marker(final String markerName,
    final double markerSize, final Color lineColor, final double lineWidth,
    final Color fillColor) {
    final AbstractMarker marker = new ShapeMarker(markerName);
    return marker(marker, markerSize, lineColor, fillColor);
  }

  public static void setMarker(final MarkerStyle style,
    final AbstractMarker marker, final double markerSize,
    final Color lineColor, final Color fillColor) {
    style.setMarker(marker);
    style.setMarkerWidth(Measure.valueOf(markerSize, NonSI.PIXEL));
    style.setMarkerHeight(Measure.valueOf(markerSize, NonSI.PIXEL));
    style.setMarkerLineColor(lineColor);
    style.setMarkerHorizontalAlignment("center");
    style.setMarkerVerticalAlignment("middle");
    style.setMarkerFill(fillColor);
  }

  public static void setMarker(final MarkerStyle style,
    final String markerName, final double markerSize, final Color lineColor,
    final double lineWidth, final Color fillColor) {
    final AbstractMarker marker = new ShapeMarker(markerName);
    setMarker(style, marker, markerSize, lineColor, fillColor);
  }

  private Marker marker = ELLIPSE;

  private boolean markerAllowOverlap;

  private boolean markerClip = true;

  private String markerCompOp;

  private Measure<Length> markerDeltaX = ZERO_PIXEL;

  private Measure<Length> markerDeltaY = ZERO_PIXEL;

  private String markerFile;

  private Resource markerFileResource;

  private Color markerFill = new Color(0, 0, 255, 255);

  private int markerFillOpacity = 255;

  private Measure<Length> markerHeight = TEN_PIXELS;

  private String markerHorizontalAlignment = "auto";

  private String markerIgnorePlacement;

  private Color markerLineColor = new Color(255, 255, 255, 255);

  private int markerLineOpacity = 255;

  private Measure<Length> markerLineWidth = ONE_PIXEL;

  private int markerOpacity = 255;

  private String markerOrientationType = "none";

  private String markerPlacement = "point";

  private double markerSmooth = 0;

  private String markerTransform;

  private String markerType = "ellipse";

  private String markerVerticalAlignment = "auto";

  private Measure<Length> markerWidth = TEN_PIXELS;

  public MarkerStyle() {
  }

  public MarkerStyle(final Map<String, Object> style) {
    for (final Entry<String, Object> entry : style.entrySet()) {
      final String propertyName = entry.getKey();
      final Object value = entry.getValue();

      final Object propertyValue = getValue(propertyName, value);
      if (propertyValue != null) {
        JavaBeanUtil.setProperty(this, propertyName, propertyValue);
      }
    }
  }

  @Override
  public MarkerStyle clone() {
    try {
      return (MarkerStyle)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  public Marker getMarker() {
    return marker;
  }

  public String getMarkerCompOp() {
    return markerCompOp;
  }

  public Measure<Length> getMarkerDeltaX() {
    return markerDeltaX;
  }

  public Measure<Length> getMarkerDeltaY() {
    return markerDeltaY;
  }

  public Measure<Length> getMarkerDx() {
    return markerDeltaX;
  }

  public Measure<Length> getMarkerDy() {
    return markerDeltaY;
  }

  public String getMarkerFile() {
    return markerFile;
  }

  public Color getMarkerFill() {
    return markerFill;
  }

  public int getMarkerFillOpacity() {
    return markerFillOpacity;
  }

  public Measure<Length> getMarkerHeight() {
    return markerHeight;
  }

  public String getMarkerHorizontalAlignment() {
    return markerHorizontalAlignment;
  }

  public String getMarkerIgnorePlacement() {
    return markerIgnorePlacement;
  }

  public Color getMarkerLineColor() {
    return markerLineColor;
  }

  public int getMarkerLineOpacity() {
    return markerLineOpacity;
  }

  public Measure<Length> getMarkerLineWidth() {
    return markerLineWidth;
  }

  public int getMarkerOpacity() {
    return markerOpacity;
  }

  public String getMarkerOrientationType() {
    return markerOrientationType;
  }

  public String getMarkerPlacement() {
    return markerPlacement;
  }

  public double getMarkerSmooth() {
    return markerSmooth;
  }

  public String getMarkerTransform() {
    return markerTransform;
  }

  public String getMarkerType() {
    return markerType;
  }

  public String getMarkerVerticalAlignment() {
    return markerVerticalAlignment;
  }

  public Measure<Length> getMarkerWidth() {
    return markerWidth;
  }

  public boolean isMarkerAllowOverlap() {
    return markerAllowOverlap;
  }

  public boolean isMarkerClip() {
    return markerClip;
  }

  public void setMarker(final Marker marker) {
    this.marker = getWithDefault(marker, ELLIPSE);
  }

  public void setMarkerAllowOverlap(final boolean markerAllowOverlap) {
    this.markerAllowOverlap = markerAllowOverlap;
  }

  public void setMarkerClip(final boolean markerClip) {
    this.markerClip = markerClip;
  }

  public void setMarkerCompOp(final String markerCompOp) {
    this.markerCompOp = markerCompOp;
  }

  public void setMarkerDeltaX(final Measure<Length> markerDx) {
    this.markerDeltaX = getWithDefault(markerDx, ZERO_PIXEL);
  }

  public void setMarkerDeltaY(final Measure<Length> markerDy) {
    this.markerDeltaY = getWithDefault(markerDy, ZERO_PIXEL);
  }

  public void setMarkerDx(final double markerDx) {
    setMarkerDeltaX(Measure.valueOf(markerDx, NonSI.PIXEL));
  }

  public void setMarkerDy(final double markerDy) {
    setMarkerDeltaY(Measure.valueOf(markerDy, NonSI.PIXEL));
  }

  public void setMarkerFile(final String markerFile) {
    this.markerFile = markerFile;
    final Pattern pattern = Pattern.compile("url\\('?([^']+)'?\\)");
    String url;
    final Matcher matcher = pattern.matcher(markerFile);
    if (matcher.find()) {
      url = matcher.group(1);
    } else {
      url = markerFile;
    }
    if (url.toUpperCase().matches("[A-Z][A-Z0-9\\+\\.\\-]*:")) {
      this.markerFileResource = SpringUtil.getUrlResource(url);
    } else {
      this.markerFileResource = SpringUtil.getBaseResource(url);
    }
    setMarker(new ImageMarker(this.markerFileResource));
  }

  public void setMarkerFill(final Color markerFill) {
    if (markerFill == null) {
      this.markerFill = new Color(128, 128, 128, markerFillOpacity);
    } else {
      this.markerFill = markerFill;
      this.markerFillOpacity = markerFill.getAlpha();
    }
  }

  public void setMarkerFillOpacity(final double markerFillOpacity) {
    if (markerFillOpacity < 0 || markerFillOpacity > 1) {
      throw new IllegalArgumentException(
        "The opacity must be between 0.0 - 1.0");
    } else {
      this.markerFillOpacity = (int)(255 * markerFillOpacity);
      this.markerFill = getColorWithOpacity(markerFill, this.markerFillOpacity);
    }
  }

  public void setMarkerFillOpacity(final int markerFillOpacity) {
    if (markerFillOpacity < 0 || markerFillOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      this.markerFillOpacity = markerFillOpacity;
      this.markerFill = getColorWithOpacity(markerFill, this.markerFillOpacity);
    }
  }

  public boolean setMarkerFillStyle(final Viewport2D viewport,
    final Graphics2D graphics) {
    if (markerFill.getAlpha() == 0) {
      return false;
    } else {
      graphics.setPaint(markerFill);
      return true;
    }
  }

  public void setMarkerHeight(final Measure<Length> markerHeight) {
    this.markerHeight = getWithDefault(markerHeight, TEN_PIXELS);
  }

  public void setMarkerHorizontalAlignment(
    final String markerHorizontalAlignment) {
    this.markerHorizontalAlignment = getWithDefault(markerHorizontalAlignment,
      "auto");
  }

  public void setMarkerIgnorePlacement(final String markerIgnorePlacement) {
    this.markerIgnorePlacement = markerIgnorePlacement;
  }

  public void setMarkerLineColor(final Color markerLineColor) {
    if (markerLineColor == null) {
      this.markerLineColor = new Color(128, 128, 128, markerLineOpacity);
    } else {
      this.markerLineColor = markerLineColor;
      this.markerLineOpacity = markerLineColor.getAlpha();
    }
  }

  public void setMarkerLineOpacity(final double markerLineOpacity) {
    if (markerLineOpacity < 0 || markerLineOpacity > 1) {
      throw new IllegalArgumentException(
        "The opacity must be between 0.0 - 1.0");
    } else {
      this.markerLineOpacity = (int)(255 * markerLineOpacity);
      this.markerLineColor = getColorWithOpacity(markerLineColor,
        this.markerLineOpacity);
    }
  }

  public void setMarkerLineOpacity(final int markerLineOpacity) {
    if (markerLineOpacity < 0 || markerLineOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      this.markerLineOpacity = markerLineOpacity;
      this.markerLineColor = getColorWithOpacity(markerLineColor,
        this.markerLineOpacity);
    }
  }

  public boolean setMarkerLineStyle(final Viewport2D viewport,
    final Graphics2D graphics) {
    final Color color = getMarkerLineColor();
    if (color.getAlpha() == 0) {
      return false;
    } else {
      graphics.setColor(color);
      final float width = (float)Viewport2D.toDisplayValue(viewport,
        markerLineWidth);
      final BasicStroke basicStroke = new BasicStroke(width);
      graphics.setStroke(basicStroke);
      return true;
    }
  }

  public void setMarkerLineWidth(final Measure<Length> markerLineWidth) {
    this.markerLineWidth = getWithDefault(markerLineWidth, ONE_PIXEL);
  }

  public void setMarkerOpacity(final double markerOpacity) {
    if (markerLineOpacity < 0 || markerLineOpacity > 1) {
      throw new IllegalArgumentException(
        "The opacity must be between 0.0 - 1.0");
    } else {
      this.markerOpacity = (int)(255 * markerOpacity);
      setMarkerLineOpacity(markerOpacity);
      setMarkerFillOpacity(markerOpacity);
    }
  }

  public void setMarkerOpacity(final int markerOpacity) {
    if (markerOpacity < 0 || markerOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      this.markerOpacity = markerOpacity;
      setMarkerLineOpacity(markerOpacity);
      setMarkerFillOpacity(markerOpacity);
    }
  }

  public void setMarkerOrientationType(final String markerOrientationType) {
    this.markerOrientationType = getWithDefault(markerOrientationType, "none");
  }

  public void setMarkerPlacement(final String markerPlacement) {
    this.markerPlacement = getWithDefault(markerPlacement, "point");
  }

  public void setMarkerSmooth(final double markerSmooth) {
    this.markerSmooth = markerSmooth;
  }

  public void setMarkerTransform(final String markerTransform) {
    this.markerTransform = markerTransform;
  }

  public void setMarkerType(final String markerType) {
    this.markerType = getWithDefault(markerType, "ellipse");
    setMarker(new ShapeMarker(this.markerType));
  }

  public void setMarkerVerticalAlignment(final String markerVerticalAlignment) {
    this.markerVerticalAlignment = getWithDefault(markerVerticalAlignment,
      "auto");
  }

  public void setMarkerWidth(final Measure<Length> markerWidth) {
    this.markerWidth = getWithDefault(markerWidth, TEN_PIXELS);
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

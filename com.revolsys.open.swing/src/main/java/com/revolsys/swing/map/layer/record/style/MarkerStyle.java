package com.revolsys.swing.map.layer.record.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Arrays;
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

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.Equals;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.marker.AbstractMarker;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.ShapeMarker;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class MarkerStyle implements Cloneable, MapSerializer {

  private static final Map<String, Object> DEFAULT_VALUES = new TreeMap<String, Object>();

  public static final AbstractMarker ELLIPSE = new ShapeMarker("ellipse");

  public static final Measure<Length> ONE_PIXEL = Measure.valueOf(1, NonSI.PIXEL);

  private static final Map<String, Class<?>> PROPERTIES = new TreeMap<String, Class<?>>();

  public static final Measure<Length> TEN_PIXELS = Measure.valueOf(10, NonSI.PIXEL);

  public static final Measure<Length> ZERO_PIXEL = Measure.valueOf(0, NonSI.PIXEL);

  static {
    addProperty("markerFile", String.class, null);
    addProperty("markerOpacity", Integer.class, 255);
    addProperty("markerFillOpacity", Integer.class, 255);
    addProperty("markerLineColor", Color.class, new Color(255, 255, 255));
    addProperty("markerLineWidth", Measure.class, ONE_PIXEL);
    addProperty("markerLineOpacity", Double.class, 255);
    addProperty("markerPlacementType", String.class, "auto");
    addProperty("markerType", String.class, "ellipse");
    addProperty("markerWidth", Measure.class, TEN_PIXELS);
    addProperty("markerHeight", Measure.class, TEN_PIXELS);
    addProperty("markerFill", Color.class, new Color(0, 0, 255));
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
    addProperty("markerOrientation", Double.class, 0.0);
    addProperty("markerOrientationType", String.class, "none");
    addProperty("markerHorizontalAlignment", String.class, "center");
    addProperty("markerVerticalAlignment", String.class, "middle");
    addProperty("markerDx", Measure.class, ZERO_PIXEL);
    addProperty("markerDy", Measure.class, ZERO_PIXEL);
  }

  protected static final void addProperty(final String name, final Class<?> dataClass,
    final Object defaultValue) {
    PROPERTIES.put(name, dataClass);
    DEFAULT_VALUES.put(name, defaultValue);
  }

  private static Object getValue(final String propertyName, final Object value) {
    final Class<?> dataClass = PROPERTIES.get(propertyName);
    if (dataClass == null) {
      return value;
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

  public static MarkerStyle marker(final AbstractMarker marker, final double markerSize,
    final Color lineColor, final Color fillColor) {
    final MarkerStyle style = new MarkerStyle();
    setMarker(style, marker, markerSize, lineColor, fillColor);
    return style;
  }

  public static MarkerStyle marker(final Shape shape, final double markerSize,
    final Color lineColor, final double lineWidth, final Color fillColor) {
    final AbstractMarker marker = new ShapeMarker(shape);
    return marker(marker, markerSize, lineColor, fillColor);
  }

  public static MarkerStyle marker(final String markerName, final double markerSize,
    final Color lineColor, final double lineWidth, final Color fillColor) {
    final AbstractMarker marker = new ShapeMarker(markerName);
    return marker(marker, markerSize, lineColor, fillColor);
  }

  public static void setMarker(final MarkerStyle style, final AbstractMarker marker,
    final double markerSize, final Color lineColor, final Color fillColor) {
    style.setMarker(marker);
    style.setMarkerWidth(Measure.valueOf(markerSize, NonSI.PIXEL));
    style.setMarkerHeight(Measure.valueOf(markerSize, NonSI.PIXEL));
    style.setMarkerLineColor(lineColor);
    style.setMarkerHorizontalAlignment("center");
    style.setMarkerVerticalAlignment("middle");
    style.setMarkerFill(fillColor);
  }

  public static void setMarker(final MarkerStyle style, final String markerName,
    final double markerSize, final Color lineColor, final double lineWidth, final Color fillColor) {
    final AbstractMarker marker = new ShapeMarker(markerName);
    setMarker(style, marker, markerSize, lineColor, fillColor);
  }

  private Marker marker = ELLIPSE;

  private boolean markerAllowOverlap;

  private boolean markerClip = true;

  private String markerCompOp;

  private Measure<Length> markerDx = ZERO_PIXEL;

  private Measure<Length> markerDy = ZERO_PIXEL;

  private String markerFile;

  private Resource markerFileResource;

  private Color markerFill = new Color(0, 0, 255, 255);

  /** The orientation of the text in a clockwise direction from the east axis. */
  private double markerOrientation = 0;

  private int markerFillOpacity = 255;

  private Measure<Length> markerHeight = TEN_PIXELS;

  private String markerHorizontalAlignment = "center";

  private String markerIgnorePlacement;

  private Color markerLineColor = new Color(255, 255, 255, 255);

  private int markerLineOpacity = 255;

  private Measure<Length> markerLineWidth = ONE_PIXEL;

  private int markerOpacity = 255;

  private String markerOrientationType = "none";

  private String markerPlacementType = "auto";

  private double markerSmooth = 0;

  private String markerTransform;

  private String markerType = "ellipse";

  private String markerVerticalAlignment = "middle";

  private Measure<Length> markerWidth = TEN_PIXELS;

  public MarkerStyle() {
  }

  public MarkerStyle(final Map<String, Object> style) {
    setStyle(style);
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
    return this.marker;
  }

  public String getMarkerCompOp() {
    return this.markerCompOp;
  }

  public Measure<Length> getMarkerDx() {
    return this.markerDx;
  }

  public Measure<Length> getMarkerDy() {
    return this.markerDy;
  }

  public String getMarkerFile() {
    return this.markerFile;
  }

  public Color getMarkerFill() {
    return this.markerFill;
  }

  public int getMarkerFillOpacity() {
    return this.markerFillOpacity;
  }

  public Measure<Length> getMarkerHeight() {
    return this.markerHeight;
  }

  public String getMarkerHorizontalAlignment() {
    return this.markerHorizontalAlignment;
  }

  public String getMarkerIgnorePlacement() {
    return this.markerIgnorePlacement;
  }

  public Color getMarkerLineColor() {
    return this.markerLineColor;
  }

  public int getMarkerLineOpacity() {
    return this.markerLineOpacity;
  }

  public Measure<Length> getMarkerLineWidth() {
    return this.markerLineWidth;
  }

  public int getMarkerOpacity() {
    return this.markerOpacity;
  }

  public double getMarkerOrientation() {
    return this.markerOrientation;
  }

  public String getMarkerOrientationType() {
    return this.markerOrientationType;
  }

  public String getMarkerPlacementType() {
    return this.markerPlacementType;
  }

  public double getMarkerSmooth() {
    return this.markerSmooth;
  }

  public String getMarkerTransform() {
    return this.markerTransform;
  }

  public String getMarkerType() {
    return this.markerType;
  }

  public String getMarkerVerticalAlignment() {
    return this.markerVerticalAlignment;
  }

  public Measure<Length> getMarkerWidth() {
    return this.markerWidth;
  }

  public boolean isMarkerAllowOverlap() {
    return this.markerAllowOverlap;
  }

  public boolean isMarkerClip() {
    return this.markerClip;
  }

  public void setMarker(final Marker marker) {
    final Object oldValue = this.marker;
    this.marker = getWithDefault(marker, ELLIPSE);
    if (marker instanceof ShapeMarker) {
      if (marker != oldValue) {
        final ShapeMarker shapeMarker = (ShapeMarker)marker;
        this.markerType = shapeMarker.getName();
      }
    } else {
      this.markerType = "ellipse";
    }
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

  public void setMarkerDx(final double markerDx) {
    setMarkerDx(Measure.valueOf(markerDx, NonSI.PIXEL));
  }

  public void setMarkerDx(final Measure<Length> markerDx) {
    this.markerDx = getWithDefault(markerDx, ZERO_PIXEL);
  }

  public void setMarkerDy(final double markerDy) {
    setMarkerDy(Measure.valueOf(markerDy, NonSI.PIXEL));
  }

  public void setMarkerDy(final Measure<Length> markerDy) {
    this.markerDy = getWithDefault(markerDy, ZERO_PIXEL);
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
      this.markerFileResource = SpringUtil.getResource(url);
    } else {
      this.markerFileResource = SpringUtil.getBaseResource(url);
    }
    setMarker(new ImageMarker(this.markerFileResource));
  }

  public void setMarkerFill(final Color markerFill) {
    if (markerFill == null) {
      this.markerFill = new Color(128, 128, 128, this.markerFillOpacity);
    } else {
      this.markerFill = markerFill;
      this.markerFillOpacity = markerFill.getAlpha();
    }
  }

  public void setMarkerFillOpacity(final double markerFillOpacity) {
    if (markerFillOpacity < 0 || markerFillOpacity > 1) {
      throw new IllegalArgumentException("The opacity must be between 0.0 - 1.0");
    } else {
      this.markerFillOpacity = (int)(255 * markerFillOpacity);
      this.markerFill = WebColors.setAlpha(this.markerFill, this.markerFillOpacity);
    }
  }

  public void setMarkerFillOpacity(final int markerFillOpacity) {
    if (markerFillOpacity < 0 || markerFillOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      this.markerFillOpacity = markerFillOpacity;
      this.markerFill = WebColors.setAlpha(this.markerFill, this.markerFillOpacity);
    }
  }

  public boolean setMarkerFillStyle(final Viewport2D viewport, final Graphics2D graphics) {
    if (this.markerFill.getAlpha() == 0) {
      return false;
    } else {
      graphics.setPaint(this.markerFill);
      return true;
    }
  }

  public void setMarkerHeight(final Measure<Length> markerHeight) {
    this.markerHeight = getWithDefault(markerHeight, TEN_PIXELS);
  }

  public void setMarkerHorizontalAlignment(final String markerHorizontalAlignment) {
    this.markerHorizontalAlignment = getWithDefault(markerHorizontalAlignment, "center");
  }

  public void setMarkerIgnorePlacement(final String markerIgnorePlacement) {
    this.markerIgnorePlacement = markerIgnorePlacement;
  }

  public void setMarkerLineColor(final Color markerLineColor) {
    if (markerLineColor == null) {
      this.markerLineColor = new Color(128, 128, 128, this.markerLineOpacity);
    } else {
      this.markerLineColor = markerLineColor;
      this.markerLineOpacity = markerLineColor.getAlpha();
    }
  }

  public void setMarkerLineOpacity(final double markerLineOpacity) {
    if (markerLineOpacity < 0 || markerLineOpacity > 1) {
      throw new IllegalArgumentException("The opacity must be between 0.0 - 1.0");
    } else {
      this.markerLineOpacity = (int)(255 * markerLineOpacity);
      this.markerLineColor = WebColors.setAlpha(this.markerLineColor, this.markerLineOpacity);
    }
  }

  public void setMarkerLineOpacity(final int markerLineOpacity) {
    if (markerLineOpacity < 0 || markerLineOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      this.markerLineOpacity = markerLineOpacity;
      this.markerLineColor = WebColors.setAlpha(this.markerLineColor, this.markerLineOpacity);
    }
  }

  public boolean setMarkerLineStyle(final Viewport2D viewport, final Graphics2D graphics) {
    final Color color = getMarkerLineColor();
    if (color.getAlpha() == 0) {
      return false;
    } else {
      graphics.setColor(color);
      final float width = (float)Viewport2D.toDisplayValue(viewport, this.markerLineWidth);
      final BasicStroke basicStroke = new BasicStroke(width);
      graphics.setStroke(basicStroke);
      return true;
    }
  }

  public void setMarkerLineWidth(final Measure<Length> markerLineWidth) {
    this.markerLineWidth = getWithDefault(markerLineWidth, ONE_PIXEL);
  }

  public void setMarkerOpacity(final double markerOpacity) {
    if (this.markerLineOpacity < 0 || this.markerLineOpacity > 1) {
      throw new IllegalArgumentException("The opacity must be between 0.0 - 1.0");
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

  public void setMarkerOrientation(final double markerOrientation) {
    this.markerOrientation = markerOrientation;
  }

  public void setMarkerOrientationType(final String markerOrientationType) {
    this.markerOrientationType = getWithDefault(markerOrientationType, "none");
  }

  public void setMarkerPlacement(final String markerPlacementType) {
    setMarkerPlacementType(markerPlacementType);
  }

  public void setMarkerPlacementType(final String markerPlacementType) {
    this.markerPlacementType = getWithDefault(markerPlacementType, "auto");
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
    this.markerVerticalAlignment = getWithDefault(markerVerticalAlignment, "middle");
  }

  public void setMarkerWidth(final Measure<Length> markerWidth) {
    this.markerWidth = getWithDefault(markerWidth, TEN_PIXELS);
  }

  protected void setStyle(final Map<String, Object> style) {
    for (final Entry<String, Object> entry : style.entrySet()) {
      final String propertyName = entry.getKey();
      if (PROPERTIES.containsKey(propertyName)) {
        Object value = entry.getValue();
        if (Arrays.asList("lineDashOffset", "lineMiterLimit").contains(propertyName)) {
          final String string = (String)value;
          value = string.replaceAll(" \\[pnt\\]", "");
        }
        final Object propertyValue = getValue(propertyName, value);
        try {
          JavaBeanUtil.setProperty(this, propertyName, propertyValue);
        } catch (final Throwable e) {
          ExceptionUtil.log(getClass(), "Unable to set style " + propertyName + "=" + propertyValue,
            e);
        }
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final boolean geometryStyle = this instanceof GeometryStyle;
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    for (final String name : PROPERTIES.keySet()) {
      if (geometryStyle || name.startsWith("marker")) {
        final Object value = Property.get(this, name);

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
    }
    return map;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }
}

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
import javax.measure.unit.Unit;

import com.revolsys.awt.WebColors;
import com.revolsys.beans.AbstractPropertyChangeSupportProxy;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.marker.AbstractMarker;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.ShapeMarker;
import com.revolsys.util.Exceptions;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class MarkerStyle extends AbstractPropertyChangeSupportProxy
  implements Cloneable, MapSerializer {

  private static final Map<String, Object> DEFAULT_VALUES = new TreeMap<>();

  public static final AbstractMarker ELLIPSE = new ShapeMarker("ellipse");

  public static final Measure<Length> ONE_PIXEL = Measure.valueOf(1, NonSI.PIXEL);

  private static final Map<String, DataType> PROPERTIES = new TreeMap<>();

  public static final Measure<Length> TEN_PIXELS = Measure.valueOf(10, NonSI.PIXEL);

  public static final Measure<Length> ZERO_PIXEL = Measure.valueOf(0, NonSI.PIXEL);

  static {
    addProperty("markerFile", DataTypes.STRING, null);
    addProperty("markerOpacity", DataTypes.INT, 255);
    addProperty("markerFillOpacity", DataTypes.INT, 255);
    addProperty("markerLineColor", DataTypes.COLOR, new Color(255, 255, 255));
    addProperty("markerLineWidth", DataTypes.MEASURE, ONE_PIXEL);
    addProperty("markerLineOpacity", DataTypes.DOUBLE, 255);
    addProperty("markerPlacementType", DataTypes.STRING, "auto");
    addProperty("markerType", DataTypes.STRING, "ellipse");
    addProperty("markerWidth", DataTypes.MEASURE, TEN_PIXELS);
    addProperty("markerHeight", DataTypes.MEASURE, TEN_PIXELS);
    addProperty("markerFill", DataTypes.COLOR, new Color(0, 0, 255));
    addProperty("markerAllowOverlap", DataTypes.BOOLEAN, false);
    addProperty("markerIgnorePlacement", DataTypes.STRING, null);
    /*
     * addProperty("markerSpacing",DataTypes.String);
     * addProperty("markerMaxError",DataTypes.String);
     */
    addProperty("markerTransform", DataTypes.STRING, null);
    addProperty("markerClip", DataTypes.BOOLEAN, true);
    addProperty("markerSmooth", DataTypes.DOUBLE, 0.0);
    addProperty("markerCompOp", DataTypes.STRING, null);
    addProperty("markerOrientation", DataTypes.DOUBLE, 0.0);
    addProperty("markerOrientationType", DataTypes.STRING, "none");
    addProperty("markerHorizontalAlignment", DataTypes.STRING, "center");
    addProperty("markerVerticalAlignment", DataTypes.STRING, "middle");
    addProperty("markerDx", DataTypes.MEASURE, ZERO_PIXEL);
    addProperty("markerDy", DataTypes.MEASURE, ZERO_PIXEL);
  }

  protected static final void addProperty(final String name, final DataType dataType,
    final Object defaultValue) {
    PROPERTIES.put(name, dataType);
    DEFAULT_VALUES.put(name, defaultValue);
  }

  private static Object getValue(final String propertyName, final Object value) {
    final DataType dataType = PROPERTIES.get(propertyName);
    if (dataType == null) {
      return value;
    } else {
      return dataType.toObject(value);
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
    style.setMarker(marker, markerSize, lineColor, fillColor);
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
    final MarkerStyle style = marker(marker, markerSize, lineColor, fillColor);
    style.setMarkerLineWidth(Measure.valueOf(lineWidth, NonSI.PIXEL));
    return style;
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

  private int markerFillOpacity = 255;

  private Measure<Length> markerHeight = TEN_PIXELS;

  private String markerHorizontalAlignment = "center";

  private String markerIgnorePlacement;

  private Color markerLineColor = new Color(255, 255, 255, 255);

  private int markerLineOpacity = 255;

  private Measure<Length> markerLineWidth = ONE_PIXEL;

  private int markerOpacity = 255;

  /** The orientation of the text in a clockwise direction from the east axis. */
  private double markerOrientation = 0;

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
    return (MarkerStyle)super.clone();
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

  public void setMarker(final AbstractMarker marker, final double markerSize, final Color lineColor,
    final Color fillColor) {
    setMarker(marker);
    setMarkerWidth(Measure.valueOf(markerSize, NonSI.PIXEL));
    setMarkerHeight(Measure.valueOf(markerSize, NonSI.PIXEL));
    setMarkerLineColor(lineColor);
    setMarkerHorizontalAlignment("center");
    setMarkerVerticalAlignment("middle");
    setMarkerFill(fillColor);
  }

  public void setMarker(final Marker marker) {
    final Marker oldMarker = this.marker;
    final String oldMarkerType = this.markerType;
    this.marker = getWithDefault(marker, ELLIPSE);
    if (marker instanceof ShapeMarker) {
      if (marker != oldMarker) {
        final ShapeMarker shapeMarker = (ShapeMarker)marker;
        this.markerType = shapeMarker.getName();
      }
    } else {
      this.markerType = "ellipse";
    }
    firePropertyChange("marker", oldMarker, this.marker);
    firePropertyChange("markerType", oldMarkerType, this.markerType);
  }

  @SuppressWarnings("unchecked")
  public <V extends MarkerStyle> V setMarker(final String markerName, final double markerSize,
    final Color lineColor, final double lineWidth, final Color fillColor) {
    final AbstractMarker marker = new ShapeMarker(markerName);
    setMarker(marker, markerSize, lineColor, fillColor);
    return (V)this;
  }

  public void setMarkerAllowOverlap(final boolean markerAllowOverlap) {
    final Object oldValue = this.markerAllowOverlap;
    this.markerAllowOverlap = markerAllowOverlap;
    firePropertyChange("markerAllowOverlap", oldValue, this.markerAllowOverlap);
  }

  public void setMarkerClip(final boolean markerClip) {
    final Object oldValue = this.markerClip;
    this.markerClip = markerClip;
    firePropertyChange("markerClip", oldValue, this.markerClip);
  }

  public void setMarkerCompOp(final String markerCompOp) {
    final Object oldValue = this.markerCompOp;
    this.markerCompOp = markerCompOp;
    firePropertyChange("markerCompOp", oldValue, this.markerCompOp);
  }

  public void setMarkerDx(final double markerDx) {
    setMarkerDx(Measure.valueOf(markerDx, NonSI.PIXEL));
  }

  public void setMarkerDx(final Measure<Length> markerDx) {
    final Object oldValue = this.markerDy;
    if (markerDx == null) {
      this.markerDx = this.markerDy;
    } else {
      this.markerDx = markerDx;
    }
    firePropertyChange("markerDx", oldValue, this.markerDx);
    updateMarkerDeltaUnits(this.markerDx.getUnit());
  }

  public void setMarkerDy(final double markerDy) {
    setMarkerDy(Measure.valueOf(markerDy, NonSI.PIXEL));
  }

  public void setMarkerDy(final Measure<Length> markerDy) {
    final Object oldValue = this.markerDy;
    if (markerDy == null) {
      this.markerDy = this.markerDx;
    } else {
      this.markerDy = markerDy;
    }
    firePropertyChange("markerDy", oldValue, this.markerDy);
    updateMarkerDeltaUnits(this.markerDy.getUnit());
  }

  public void setMarkerFile(final String markerFile) {
    final Object oldMarkerFile = this.markerFile;
    // TODO property change
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
      this.markerFileResource = Resource.getResource(url);
    } else {
      this.markerFileResource = SpringUtil.getBaseResource(url);
    }
    firePropertyChange("markerFile", oldMarkerFile, markerFile);
    setMarker(new ImageMarker(this.markerFileResource));
  }

  public void setMarkerFill(final Color markerFill) {
    final Object oldMarkerFill = this.markerFill;
    final Object oldMarkerFillOpacity = this.markerFillOpacity;
    if (markerFill == null) {
      this.markerFill = new Color(128, 128, 128, this.markerFillOpacity);
    } else {
      this.markerFill = markerFill;
      this.markerFillOpacity = markerFill.getAlpha();
    }
    firePropertyChange("markerFill", oldMarkerFill, this.markerFill);
    firePropertyChange("markerFillOpacity", oldMarkerFillOpacity, this.markerFillOpacity);
  }

  public void setMarkerFillOpacity(final double markerFillOpacity) {
    if (markerFillOpacity < 0 || markerFillOpacity > 1) {
      throw new IllegalArgumentException("The opacity must be between 0.0 - 1.0");
    } else {
      setMarkerFillOpacity((int)(255 * markerFillOpacity));
    }
  }

  public void setMarkerFillOpacity(final int markerFillOpacity) {
    if (markerFillOpacity < 0 || markerFillOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      final Object oldMarkerFill = this.markerFill;
      final Object oldMarkerFillOpacity = this.markerFillOpacity;
      this.markerFillOpacity = markerFillOpacity;
      this.markerFill = WebColors.setAlpha(this.markerFill, this.markerFillOpacity);
      firePropertyChange("markerFill", oldMarkerFill, this.markerFill);
      firePropertyChange("markerFillOpacity", oldMarkerFillOpacity, this.markerFillOpacity);
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
    final Object oldValue = this.markerHeight;
    if (markerHeight == null) {
      this.markerHeight = this.markerWidth;
    } else {
      this.markerHeight = markerHeight;
    }
    firePropertyChange("markerHeight", oldValue, this.markerHeight);
    updateMarkerUnits(this.markerHeight.getUnit());
  }

  public void setMarkerHorizontalAlignment(final String markerHorizontalAlignment) {
    final Object oldValue = this.markerHorizontalAlignment;
    this.markerHorizontalAlignment = getWithDefault(markerHorizontalAlignment, "center");
    firePropertyChange("markerHorizontalAlignment", oldValue, this.markerHorizontalAlignment);
  }

  public void setMarkerIgnorePlacement(final String markerIgnorePlacement) {
    final Object oldValue = this.markerIgnorePlacement;
    this.markerIgnorePlacement = markerIgnorePlacement;
    firePropertyChange("markerIgnorePlacement", oldValue, this.markerIgnorePlacement);
  }

  public void setMarkerLineColor(final Color markerLineColor) {
    final Object oldMarkerLineColor = this.markerLineColor;
    final Object oldMarkerLineOpacity = this.markerLineOpacity;
    if (markerLineColor == null) {
      this.markerLineColor = new Color(128, 128, 128, this.markerLineOpacity);
    } else {
      this.markerLineColor = markerLineColor;
      this.markerLineOpacity = markerLineColor.getAlpha();
    }
    firePropertyChange("markerLineColor", oldMarkerLineColor, this.markerLineColor);
    firePropertyChange("markerLineOpacity", oldMarkerLineOpacity, this.markerLineOpacity);
  }

  public void setMarkerLineOpacity(final double markerLineOpacity) {
    if (markerLineOpacity < 0 || markerLineOpacity > 1) {
      throw new IllegalArgumentException("The opacity must be between 0.0 - 1.0");
    } else {
      setMarkerLineOpacity((int)(255 * markerLineOpacity));
    }
  }

  public void setMarkerLineOpacity(final int markerLineOpacity) {
    if (markerLineOpacity < 0 || markerLineOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      final Object oldMarkerLineColor = this.markerLineColor;
      final Object oldMarkerLineOpacity = this.markerLineOpacity;
      this.markerLineOpacity = markerLineOpacity;
      this.markerLineColor = WebColors.setAlpha(this.markerLineColor, this.markerLineOpacity);
      firePropertyChange("markerLineColor", oldMarkerLineColor, this.markerLineColor);
      firePropertyChange("markerLineOpacity", oldMarkerLineOpacity, this.markerLineOpacity);
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
    final Object oldValue = this.markerLineWidth;
    if (markerLineWidth == null) {
      this.markerLineWidth = Measure.valueOf(1, this.markerWidth.getUnit());
    } else {
      this.markerLineWidth = markerLineWidth;
    }
    firePropertyChange("markerLineWidth", oldValue, this.markerLineWidth);
    updateMarkerUnits(this.markerLineWidth.getUnit());
  }

  public void setMarkerOpacity(final double markerOpacity) {
    if (markerOpacity < 0 || markerOpacity > 1) {
      throw new IllegalArgumentException("The opacity must be between 0.0 - 1.0");
    } else {
      setMarkerOpacity((int)(255 * markerOpacity));
    }
  }

  public void setMarkerOpacity(final int markerOpacity) {
    if (markerOpacity < 0 || markerOpacity > 255) {
      throw new IllegalArgumentException("The opacity must be between 0 - 255");
    } else {
      final Object oldValue = this.markerOpacity;
      this.markerOpacity = markerOpacity;
      firePropertyChange("markerOpacity", oldValue, this.markerOpacity);
      setMarkerLineOpacity(markerOpacity);
      setMarkerFillOpacity(markerOpacity);
    }
  }

  public void setMarkerOrientation(final double markerOrientation) {
    final Object oldValue = this.markerOrientation;
    this.markerOrientation = markerOrientation;
    firePropertyChange("markerOrientation", oldValue, this.markerOrientation);
  }

  public void setMarkerOrientationType(final String markerOrientationType) {
    final Object oldValue = this.markerOrientationType;
    this.markerOrientationType = getWithDefault(markerOrientationType, "none");
    firePropertyChange("markerOrientationType", oldValue, this.markerOrientationType);
  }

  public void setMarkerPlacement(final String markerPlacementType) {
    setMarkerPlacementType(markerPlacementType);
  }

  public void setMarkerPlacementType(final String markerPlacementType) {
    final Object oldValue = this.markerPlacementType;
    this.markerPlacementType = getWithDefault(markerPlacementType, "auto");
    firePropertyChange("markerPlacementType", oldValue, this.markerPlacementType);
  }

  public void setMarkerSmooth(final double markerSmooth) {
    final Object oldValue = this.markerSmooth;
    this.markerSmooth = markerSmooth;
    firePropertyChange("markerSmooth", oldValue, this.markerSmooth);
  }

  public void setMarkerTransform(final String markerTransform) {
    final Object oldValue = this.markerTransform;
    this.markerTransform = markerTransform;
    firePropertyChange("markerTransform", oldValue, this.markerTransform);
  }

  public void setMarkerType(final String markerType) {
    final Object oldValue = this.markerType;
    this.markerType = getWithDefault(markerType, "ellipse");
    firePropertyChange("markerType", oldValue, this.markerType);
    setMarker(new ShapeMarker(this.markerType));
  }

  public void setMarkerVerticalAlignment(final String markerVerticalAlignment) {
    final Object oldValue = this.markerVerticalAlignment;
    this.markerVerticalAlignment = getWithDefault(markerVerticalAlignment, "middle");
    firePropertyChange("markerVerticalAlignment", oldValue, this.markerVerticalAlignment);
  }

  public void setMarkerWidth(final Measure<Length> markerWidth) {
    final Object oldValue = this.markerWidth;
    if (markerWidth == null) {
      this.markerWidth = this.markerHeight;
    } else {
      this.markerWidth = markerWidth;
    }
    firePropertyChange("markerWidth", oldValue, this.markerWidth);
    updateMarkerUnits(this.markerWidth.getUnit());
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
          Exceptions.log(getClass(), "Unable to set style " + propertyName + "=" + propertyValue,
            e);
        }
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final boolean geometryStyle = this instanceof GeometryStyle;
    final Map<String, Object> map = new LinkedHashMap<>();
    for (final String name : PROPERTIES.keySet()) {
      if (geometryStyle || name.startsWith("marker")) {
        final Object value = Property.get(this, name);

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
    }
    return map;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }

  private void updateMarkerDeltaUnits(final Unit<Length> unit) {
    if (!this.markerDx.getUnit().equals(unit)) {
      final double oldValue = this.markerDx.getValue().doubleValue();
      final Measure<Length> newValue = Measure.valueOf(oldValue, unit);
      setMarkerDx(newValue);
    }
    if (!this.markerDy.getUnit().equals(unit)) {
      final double oldValue = this.markerDy.getValue().doubleValue();
      final Measure<Length> newValue = Measure.valueOf(oldValue, unit);
      setMarkerDy(newValue);
    }
  }

  private void updateMarkerUnits(final Unit<Length> unit) {
    if (!this.markerWidth.getUnit().equals(unit)) {
      final double oldValue = this.markerWidth.getValue().doubleValue();
      final Measure<Length> newValue = Measure.valueOf(oldValue, unit);
      setMarkerWidth(newValue);
    }
    if (!this.markerHeight.getUnit().equals(unit)) {
      final double oldValue = this.markerHeight.getValue().doubleValue();
      final Measure<Length> newValue = Measure.valueOf(oldValue, unit);
      setMarkerHeight(newValue);
    }
    if (!this.markerLineWidth.getUnit().equals(unit)) {
      final double oldValue = this.markerLineWidth.getValue().doubleValue();
      final Measure<Length> newValue = Measure.valueOf(oldValue, unit);
      setMarkerLineWidth(newValue);
    }
  }
}

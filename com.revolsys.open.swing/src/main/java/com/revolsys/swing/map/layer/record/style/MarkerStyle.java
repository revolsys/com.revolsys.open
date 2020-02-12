package com.revolsys.swing.map.layer.record.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.swing.Icon;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.layer.record.style.marker.AbstractMarker;
import com.revolsys.swing.map.layer.record.style.marker.GeometryMarker;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.MarkerLibrary;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

import tech.units.indriya.quantity.Quantities;

public class MarkerStyle extends BaseObjectWithPropertiesAndChange
  implements Cloneable, MapSerializer {

  private static final Map<String, Object> DEFAULT_VALUES = new TreeMap<>();

  public static final Marker ELLIPSE = MarkerLibrary.findMarker("ellipse");

  public static final Quantity<Length> ONE_PIXEL = Quantities.getQuantity(1, CustomUnits.PIXEL);

  private static final Set<String> PROPERTY_NAMES = new TreeSet<>();

  public static final Quantity<Length> TEN_PIXELS = Quantities.getQuantity(10, CustomUnits.PIXEL);

  public static final Quantity<Length> ZERO_PIXEL = Quantities.getQuantity(0, CustomUnits.PIXEL);

  static {
    addStyleProperty("marker", null);
    addStyleProperty("markerAllowOverlap", false);
    addStyleProperty("markerClip", true);
    addStyleProperty("markerCompOp", null);
    addStyleProperty("markerDx", ZERO_PIXEL);
    addStyleProperty("markerDy", ZERO_PIXEL);
    addStyleProperty("markerFile", null);
    addStyleProperty("markerFill", new Color(0, 0, 255));
    addStyleProperty("markerFillOpacity", 255);
    addStyleProperty("markerHeight", TEN_PIXELS);
    addStyleProperty("markerHorizontalAlignment", "center");
    addStyleProperty("markerIgnorePlacement", null);
    addStyleProperty("markerLineColor", new Color(255, 255, 255));
    addStyleProperty("markerLineOpacity", 255);
    addStyleProperty("markerLineWidth", ONE_PIXEL);
    addStyleProperty("markerOpacity", 255);
    addStyleProperty("markerOrientation", 0.0);
    addStyleProperty("markerOrientationType", "auto");
    addStyleProperty("markerPlacementType", "auto");
    addStyleProperty("markerSmooth", 0.0);
    addStyleProperty("markerTransform", null);
    addStyleProperty("markerType", "ellipse");
    addStyleProperty("markerVerticalAlignment", "middle");
    addStyleProperty("markerWidth", TEN_PIXELS);
  }

  protected static final void addStyleProperty(final String name, final Object defaultValue) {
    PROPERTY_NAMES.add(name);
    if (defaultValue != null) {
      DEFAULT_VALUES.put(name, defaultValue);
    }
  }

  public static <T> T getWithDefault(final T value, final T defaultValue) {
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static MarkerStyle marker(final BiFunctionDouble<Geometry> newMarkerFunction,
    final double markerSize, final Color lineColor, final double lineWidth, final Color fillColor) {
    final AbstractMarker marker = new GeometryMarker(newMarkerFunction);
    return marker(marker, markerSize, lineColor, fillColor);
  }

  public static MarkerStyle marker(final Marker marker, final double markerSize,
    final Color lineColor, final Color fillColor) {
    final MarkerStyle style = new MarkerStyle();
    style.setMarker(marker, markerSize, lineColor, fillColor);
    return style;
  }

  public static MarkerStyle marker(final String markerName, final double markerSize,
    final Color lineColor, final double lineWidth, final Color fillColor) {
    final Marker marker = MarkerLibrary.findMarker(markerName);
    final MarkerStyle style = marker(marker, markerSize, lineColor, fillColor);
    style.setMarkerLineWidth(Quantities.getQuantity(lineWidth, CustomUnits.PIXEL));
    return style;
  }

  private Marker marker = ELLIPSE;

  private boolean markerAllowOverlap;

  private boolean markerClip = true;

  private String markerCompOp;

  private Quantity<Length> markerDx = ZERO_PIXEL;

  private Quantity<Length> markerDy = ZERO_PIXEL;

  private String markerFile;

  private Resource markerFileResource;

  private Color markerFill = new Color(0, 0, 255, 255);

  private int markerFillOpacity = 255;

  private Quantity<Length> markerHeight = TEN_PIXELS;

  private String markerHorizontalAlignment = "center";

  private String markerIgnorePlacement;

  private Color markerLineColor = new Color(255, 255, 255, 255);

  private int markerLineOpacity = 255;

  private Quantity<Length> markerLineWidth = ONE_PIXEL;

  private int markerOpacity = 255;

  /** The orientation of the text in a clockwise direction from the east axis. */
  private double markerOrientation = 0;

  private String markerOrientationType = "auto";

  private String markerPlacementType = "auto";

  private double markerSmooth = 0;

  private String markerTransform;

  private String markerType = "ellipse";

  private String markerVerticalAlignment = "middle";

  private Quantity<Length> markerWidth = TEN_PIXELS;

  public MarkerStyle() {
  }

  public MarkerStyle(final Map<String, Object> style) {
    setProperties(style);
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

  public Quantity<Length> getMarkerDx() {
    return this.markerDx;
  }

  public Quantity<Length> getMarkerDy() {
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

  public Quantity<Length> getMarkerHeight() {
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

  public Quantity<Length> getMarkerLineWidth() {
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

  public Quantity<Length> getMarkerWidth() {
    return this.markerWidth;
  }

  public boolean isMarkerAllowOverlap() {
    return this.markerAllowOverlap;
  }

  public boolean isMarkerClip() {
    return this.markerClip;
  }

  public Icon newIcon() {
    return this.marker.newIcon(this);
  }

  public MarkerRenderer newMarkerRenderer(final ViewRenderer view) {
    return this.marker.newMarkerRenderer(view, this);
  }

  public void setMarker(final Marker marker) {
    final Marker oldMarker = this.marker;
    final String oldMarkerType = this.markerType;
    this.marker = getWithDefault(marker, ELLIPSE);
    if (marker != null && marker.isUseMarkerName()) {
      this.markerType = marker.getName();
    } else {
      this.markerType = "ellipse";
    }
    firePropertyChange("marker", oldMarker, this.marker);
    firePropertyChange("markerType", oldMarkerType, this.markerType);
  }

  public void setMarker(final Marker marker, final double markerSize, final Color lineColor,
    final Color fillColor) {
    setMarker(marker);
    setMarkerWidth(Quantities.getQuantity(markerSize, CustomUnits.PIXEL));
    setMarkerHeight(Quantities.getQuantity(markerSize, CustomUnits.PIXEL));
    setMarkerLineColor(lineColor);
    setMarkerHorizontalAlignment("center");
    setMarkerVerticalAlignment("middle");
    setMarkerFill(fillColor);
  }

  @SuppressWarnings("unchecked")
  public <V extends MarkerStyle> V setMarker(final String markerName, final double markerSize,
    final Color lineColor, final double lineWidth, final Color fillColor) {
    final Marker marker = MarkerLibrary.findMarker(markerName);
    setMarker(marker, markerSize, lineColor, fillColor);
    setMarkerLineWidth(Quantities.getQuantity(lineWidth, CustomUnits.PIXEL));
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
    setMarkerDx(Quantities.getQuantity(markerDx, CustomUnits.PIXEL));
  }

  public void setMarkerDx(final Quantity<Length> markerDx) {
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
    setMarkerDy(Quantities.getQuantity(markerDy, CustomUnits.PIXEL));
  }

  public void setMarkerDy(final Quantity<Length> markerDy) {
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
      this.markerFileResource = Resource.getBaseResource(url);
    }
    firePropertyChange("markerFile", oldMarkerFile, markerFile);
    setMarker(new ImageMarker(this.markerFileResource));
  }

  public MarkerStyle setMarkerFill(final Color markerFill) {
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
    return this;
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
      this.markerFill = WebColors.newAlpha(this.markerFill, this.markerFillOpacity);
      firePropertyChange("markerFill", oldMarkerFill, this.markerFill);
      firePropertyChange("markerFillOpacity", oldMarkerFillOpacity, this.markerFillOpacity);
    }
  }

  public boolean setMarkerFillStyle(final ViewRenderer view, final Graphics2D graphics) {
    if (this.markerFill.getAlpha() == 0) {
      return false;
    } else {
      graphics.setPaint(this.markerFill);
      return true;
    }
  }

  public void setMarkerHeight(final Quantity<Length> markerHeight) {
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

  public MarkerStyle setMarkerLineColor(final Color markerLineColor) {
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
    return this;
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
      this.markerLineColor = WebColors.newAlpha(this.markerLineColor, this.markerLineOpacity);
      firePropertyChange("markerLineColor", oldMarkerLineColor, this.markerLineColor);
      firePropertyChange("markerLineOpacity", oldMarkerLineOpacity, this.markerLineOpacity);
    }
  }

  public void setMarkerLineWidth(final Quantity<Length> markerLineWidth) {
    final Object oldValue = this.markerLineWidth;
    if (markerLineWidth == null) {
      this.markerLineWidth = Quantities.getQuantity(1, this.markerWidth.getUnit());
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
    this.markerOrientationType = getWithDefault(markerOrientationType, "auto");
    firePropertyChange("markerOrientationType", oldValue, this.markerOrientationType);
  }

  public void setMarkerPlacement(final String markerPlacementType) {
    setMarkerPlacementType(markerPlacementType);
  }

  public void setMarkerPlacementType(String markerPlacementType) {
    final Object oldValue = this.markerPlacementType;
    markerPlacementType = Strings.replaceAll(markerPlacementType, "^point\\(", "vertex\\(");
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
    final Marker marker = MarkerLibrary.newMarker(markerType);
    setMarker(marker);
  }

  public void setMarkerVerticalAlignment(final String markerVerticalAlignment) {
    final Object oldValue = this.markerVerticalAlignment;
    this.markerVerticalAlignment = getWithDefault(markerVerticalAlignment, "middle");
    firePropertyChange("markerVerticalAlignment", oldValue, this.markerVerticalAlignment);
  }

  public void setMarkerWidth(final Quantity<Length> markerWidth) {
    final Object oldValue = this.markerWidth;
    if (markerWidth == null) {
      this.markerWidth = this.markerHeight;
    } else {
      this.markerWidth = markerWidth;
    }
    firePropertyChange("markerWidth", oldValue, this.markerWidth);
    updateMarkerUnits(this.markerWidth.getUnit());
  }

  @Override
  public void setPropertyError(final String name, final Object value, final Throwable e) {
    Logs.error(this, "Error setting " + name + '=' + value, e);
  }

  @Override
  public JsonObject toMap() {
    final boolean geometryStyle = this instanceof GeometryStyle;
    final JsonObject map = new JsonObjectHash();
    for (final String name : PROPERTY_NAMES) {
      if (geometryStyle || name.startsWith("marker")) {
        final Object value = Property.get(this, name);

        boolean defaultEqual = false;
        if (DEFAULT_VALUES.containsKey(name)) {
          final Object defaultValue = DEFAULT_VALUES.get(name);
          defaultEqual = DataType.equal(defaultValue, value);
        }
        if (!defaultEqual) {
          addToMap(map, name, value);
        }
      }
    }
    if (this.marker != null && this.marker.isUseMarkerName()) {
      map.remove("marker");
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
      final Quantity<Length> newValue = Quantities.getQuantity(oldValue, unit);
      setMarkerDx(newValue);
    }
    if (!this.markerDy.getUnit().equals(unit)) {
      final double oldValue = this.markerDy.getValue().doubleValue();
      final Quantity<Length> newValue = Quantities.getQuantity(oldValue, unit);
      setMarkerDy(newValue);
    }
  }

  private void updateMarkerUnits(final Unit<Length> unit) {
    if (!this.markerWidth.getUnit().equals(unit)) {
      final double oldValue = this.markerWidth.getValue().doubleValue();
      final Quantity<Length> newValue = Quantities.getQuantity(oldValue, unit);
      setMarkerWidth(newValue);
    }
    if (!this.markerHeight.getUnit().equals(unit)) {
      final double oldValue = this.markerHeight.getValue().doubleValue();
      final Quantity<Length> newValue = Quantities.getQuantity(oldValue, unit);
      setMarkerHeight(newValue);
    }
    if (!this.markerLineWidth.getUnit().equals(unit)) {
      final double oldValue = this.markerLineWidth.getValue().doubleValue();
      final Quantity<Length> newValue = Quantities.getQuantity(oldValue, unit);
      setMarkerLineWidth(newValue);
    }
  }
}

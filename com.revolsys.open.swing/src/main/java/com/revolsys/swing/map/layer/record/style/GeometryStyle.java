package com.revolsys.swing.map.layer.record.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.math.MathUtil;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;

import tech.units.indriya.quantity.Quantities;

public class GeometryStyle extends MarkerStyle {

  private static int colorIndex = -1;

  public static final List<Color> COLORS = Arrays.asList(WebColors.Maroon, WebColors.Olive,
    WebColors.Green, WebColors.Teal, WebColors.Navy, WebColors.Purple, WebColors.Red,
    WebColors.Yellow, WebColors.Lime, WebColors.Aqua, WebColors.Blue, WebColors.Fuchsia);

  static {
    // addStyleProperty("backgroundColor", Color.class);
    // addStyleProperty("backgroundImage", String.class);
    // addStyleProperty("base", String.class);
    // addStyleProperty("bufferSize", Double.class);
    // addStyleProperty("buildingFill", Color.class);
    // addStyleProperty("buildingFillOpacity", String.class);
    // addStyleProperty("buildingHeight", Double.class);
    // addStyleProperty("compOp", String.class);
    // addStyleProperty("fontDirectory", String.class);
    // addStyleProperty("imageFilters", String.class);
    addStyleProperty("lineCap", LineCap.ROUND);
    addStyleProperty("lineClip", true);
    addStyleProperty("lineColor", new Color(128, 128, 128));
    addStyleProperty("lineCompOp", CompositionOperation.src_over);
    addStyleProperty("lineDashOffset", 0);
    addStyleProperty("lineDashArray", Collections.emptyList());
    addStyleProperty("lineGamma", 1.0);
    addStyleProperty("lineGammaMethod", GammaMethod.power);
    addStyleProperty("lineJoin", LineJoin.ROUND);
    addStyleProperty("lineMiterlimit", 4f);
    // addStyleProperty("lineOffset", String.class);
    addStyleProperty("lineOpacity", 255);
    // addStyleProperty("linePattern", String.class);
    // addStyleProperty("linePatternClip", String.class);
    // addStyleProperty("linePatternCompOp", String.class);
    // addStyleProperty("linePatternFile", String.class);
    // addStyleProperty("linePatternSmooth", String.class);
    // addStyleProperty("lineRasterizer", String.class);
    addStyleProperty("lineSmooth", 0.0);
    addStyleProperty("lineWidth", ONE_PIXEL);
    // addStyleProperty("opacity", String.class);
    // addStyleProperty("point", String.class);
    // addStyleProperty("pointAllowOverlap", String.class);
    // addStyleProperty("pointCompOp", String.class);
    // addStyleProperty("pointFile", String.class);
    // addStyleProperty("pointIgnorePlacement", String.class);
    // addStyleProperty("pointOpacity", String.class);
    // addStyleProperty("pointPlacement", String.class);
    // addStyleProperty("pointTransform", String.class);
    // addStyleProperty("polygon", String.class);
    addStyleProperty("polygonClip", true);
    addStyleProperty("polygonCompOp", CompositionOperation.src_over);
    addStyleProperty("polygonFill", new Color(128, 128, 128));
    addStyleProperty("polygonFillOpacity", 255);
    addStyleProperty("polygonGamma", 1.0);
    addStyleProperty("polygonGammaMethod", GammaMethod.power);
    // addStyleProperty("polygonPattern", String.class);
    // addStyleProperty("polygonPatternAlignment", String.class);
    // addStyleProperty("polygonPatternClip", String.class);
    // addStyleProperty("polygonPatternCompOp", String.class);
    // addStyleProperty("polygonPatternFile", String.class);
    // addStyleProperty("polygonPatternGamma", String.class);
    // addStyleProperty("polygonPatternOpacity", String.class);
    // addStyleProperty("polygonPatternSmooth", String.class);
    addStyleProperty("polygonSmooth", 0.0);
    // addStyleProperty("raster", String.class);
    // addStyleProperty("rasterCompOp", String.class);
    // addStyleProperty("rasterFilterFactor", String.class);
    // addStyleProperty("rasterMeshSize", Double.class);
    // addStyleProperty("rasterOpacity", String.class);
    // addStyleProperty("rasterScaling", String.class);
    // addStyleProperty("shield", String.class);
    // addStyleProperty("shieldAllowOverlap", String.class);
    // addStyleProperty("shieldAvoidEdges", String.class);
    // addStyleProperty("shieldCharacterSpacing", String.class);
    // addStyleProperty("shieldClip", String.class);
    // addStyleProperty("shieldCompOp", String.class);
    // addStyleProperty("shieldDx", Double.class);
    // addStyleProperty("shieldDy", Double.class);
    // addStyleProperty("shieldFaceName", String.class);
    // addStyleProperty("shieldFile", String.class);
    // addStyleProperty("shieldFill", Color.class);
    // addStyleProperty("shieldHaloFill", Color.class);
    // addStyleProperty("shieldHaloRadius", String.class);
    // addStyleProperty("shieldHorizontalAlignment", String.class);
    // addStyleProperty("shieldJustifyAlignment", String.class);
    // addStyleProperty("shieldLineSpacing", String.class);
    // addStyleProperty("shieldMinDistance", String.class);
    // addStyleProperty("shieldMinPadding", String.class);
    // addStyleProperty("shieldName", String.class);
    // addStyleProperty("shieldOpacity", String.class);
    // addStyleProperty("shieldPlacement", String.class);
    // addStyleProperty("shieldSize", Double.class);
    // addStyleProperty("shieldSpacing", String.class);
    // addStyleProperty("shieldTextDx", Double.class);
    // addStyleProperty("shieldTextDy", Double.class);
    // addStyleProperty("shieldTextOpacity", String.class);
    // addStyleProperty("shieldTextTransform", String.class);
    // addStyleProperty("shieldverticalAlignment", String.class);
    // addStyleProperty("shieldWrapBefore", String.class);
    // addStyleProperty("shieldWrapCharacter", String.class);
    // addStyleProperty("shieldWrapWidth", Double.class);
    // addStyleProperty("srs", String.class);

  }

  public static final List<Double> DOT = newDash(2.0);

  public static final List<Double> DASH_5 = newDash(5.0);

  public static final List<Double> DASH_10 = newDash(10.0);

  public static final List<Double> DASH_15 = newDash(10.0);

  public static final List<Double> DASH_DOT_DOT = newDash(8.0, 3.0, 3.0, 3.0, 3.0, 3.0);

  public static final List<Double> DASH_DOT = newDash(8.0, 3.0, 3.0, 3.0);

  public static GeometryStyle line(final Color color) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(color);
    return style;
  }

  public static GeometryStyle line(final Color color, final double lineWidth) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(color);
    style.setLineWidth(Quantities.getQuantity(lineWidth, CustomUnits.PIXEL));
    return style;
  }

  public static List<Double> newDash(final Double... dashArray) {
    return Arrays.asList(dashArray);
  }

  public static GeometryStyle newStyle() {
    final GeometryStyle style = new GeometryStyle();
    Color color;
    synchronized (COLORS) {
      colorIndex = (colorIndex + 1) % COLORS.size();
      color = COLORS.get(colorIndex);
    }
    style.setLineColor(color);
    style.setPolygonFill(WebColors.newAlpha(color, 127));
    return style;
  }

  public static GeometryStyle polygon(final Color lineColor, final Color fillColor) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(lineColor);
    style.setPolygonFill(fillColor);
    return style;
  }

  public static GeometryStyle polygon(final Color lineColor, final int lineWidth,
    final Color fillColor) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(lineColor);
    style.setLineWidth(Quantities.getQuantity(lineWidth, CustomUnits.PIXEL));
    style.setPolygonFill(fillColor);
    return style;
  }

  private LineCap lineCap = LineCap.ROUND;

  private boolean lineClip = true;

  private Color lineColor = new Color(128, 128, 128, 255);

  private CompositionOperation lineCompOp = CompositionOperation.src_over;

  private List<Double> lineDashArray = Collections.emptyList();

  private double lineDashOffset = 0;

  private double lineGamma = 1.0;

  private GammaMethod lineGammaMethod = GammaMethod.power;

  private LineJoin lineJoin = LineJoin.ROUND;

  private float lineMiterlimit = 4;

  private int lineOpacity = 255;

  private double lineSmooth;

  private Quantity<Length> lineWidth = ONE_PIXEL;

  private boolean polygonClip = true;

  private CompositionOperation polygonCompOp = CompositionOperation.src_over;

  private Color polygonFill = new Color(128, 128, 128, 255);

  private int polygonFillOpacity = 255;

  private double polygonGamma = 1.0;

  private GammaMethod polygonGammaMethod = GammaMethod.power;

  private double polygonSmooth;

  public GeometryStyle() {
  }

  public GeometryStyle(final Map<String, Object> style) {
    setProperties(style);
  }

  public void applyLineStyle(final ViewRenderer view, final Graphics2D graphics) {
    final Color color = getLineColor();
    graphics.setColor(color);
    final Unit<Length> unit = this.lineWidth.getUnit();
    final float width = (float)view.toModelValue(this.lineWidth);

    final float dashOffset = (float)view
      .toModelValue(Quantities.getQuantity(this.lineDashOffset, unit));

    final float[] dashArray;
    final int dashArraySize = this.lineDashArray.size();
    if (dashArraySize == 0) {
      dashArray = null;
    } else {
      dashArray = new float[dashArraySize];
      for (int i = 0; i < dashArray.length; i++) {
        final Double dashDouble = this.lineDashArray.get(i);
        final float dashFloat = (float)view.toModelValue(Quantities.getQuantity(dashDouble, unit));
        dashArray[i] = dashFloat;
      }
    }

    final int lineCap = this.lineCap.getAwtValue();
    final int lineJoin = this.lineJoin.getAwtValue();
    final BasicStroke basicStroke = new BasicStroke(width, lineCap, lineJoin, this.lineMiterlimit,
      dashArray, dashOffset);
    graphics.setStroke(basicStroke);
  }

  @Override
  public GeometryStyle clone() {
    return (GeometryStyle)super.clone();
  }

  public LineCap getLineCap() {
    return this.lineCap;
  }

  public Color getLineColor() {
    return this.lineColor;
  }

  public CompositionOperation getLineCompOp() {
    return this.lineCompOp;
  }

  public List<Double> getLineDashArray() {
    return this.lineDashArray;
  }

  public double getLineDashOffset() {
    return this.lineDashOffset;
  }

  public double getLineGamma() {
    return this.lineGamma;
  }

  public GammaMethod getLineGammaMethod() {
    return this.lineGammaMethod;
  }

  public LineJoin getLineJoin() {
    return this.lineJoin;
  }

  public float getLineMiterlimit() {
    return this.lineMiterlimit;
  }

  public int getLineOpacity() {
    return this.lineOpacity;
  }

  public double getLineSmooth() {
    return this.lineSmooth;
  }

  public Quantity<Length> getLineWidth() {
    return this.lineWidth;
  }

  public CompositionOperation getPolygonCompOp() {
    return this.polygonCompOp;
  }

  public Color getPolygonFill() {
    return this.polygonFill;
  }

  public int getPolygonFillOpacity() {
    return this.polygonFillOpacity;
  }

  public double getPolygonGamma() {
    return this.polygonGamma;
  }

  public GammaMethod getPolygonGammaMethod() {
    return this.polygonGammaMethod;
  }

  public double getPolygonSmooth() {
    return this.polygonSmooth;
  }

  public boolean isLineClip() {
    return this.lineClip;
  }

  public boolean isPolygonClip() {
    return this.polygonClip;
  }

  public GeometryStyle setFill(final Color fill) {
    setPolygonFill(fill);
    setMarkerFill(fill);
    return this;
  }

  public GeometryStyle setLineCap(final LineCap lineCap) {
    final Object oldValue = this.lineCap;
    this.lineCap = lineCap;
    firePropertyChange("lineCap", oldValue, this.lineCap);
    return this;
  }

  public GeometryStyle setLineClip(final boolean lineClip) {
    final Object oldValue = this.lineClip;
    this.lineClip = lineClip;
    firePropertyChange("lineClip", oldValue, this.lineClip);
    return this;
  }

  public GeometryStyle setLineColor(final Color lineColor) {
    final Object oldLineColor = this.lineColor;
    final Object oldLineOpacity = this.lineOpacity;
    if (lineColor == null) {
      this.lineColor = new Color(128, 128, 128, this.lineOpacity);
    } else {
      this.lineColor = lineColor;
      this.lineOpacity = lineColor.getAlpha();
    }
    firePropertyChange("lineColor", oldLineColor, this.lineColor);
    firePropertyChange("lineOpacity", oldLineOpacity, this.lineOpacity);
    return this;
  }

  public GeometryStyle setLineCompOp(final CompositionOperation lineCompOp) {
    final Object oldValue = this.lineCompOp;
    this.lineCompOp = getWithDefault(lineCompOp, CompositionOperation.src_over);
    firePropertyChange("lineCompOp", oldValue, this.lineCompOp);
    return this;
  }

  public GeometryStyle setLineDashArray(final List<?> lineDashArray) {
    final Object oldValue = this.lineDashArray;
    final List<Double> dashArray = new ArrayList<>();
    if (lineDashArray != null) {
      for (final Object dashObject : lineDashArray) {
        if (Property.hasValue(dashObject)) {
          String dashString = dashObject.toString();
          dashString = dashString.replaceAll(" \\[pnt\\]", "");
          final Double dash = MathUtil.toDouble(dashString);
          dashArray.add(dash);
        }
      }
    }
    this.lineDashArray = dashArray;
    firePropertyChange("lineDashArray", oldValue, this.lineDashArray);
    return this;
  }

  public GeometryStyle setLineDashOffset(final double lineDashOffset) {
    final Object oldValue = this.lineDashOffset;
    this.lineDashOffset = lineDashOffset;
    firePropertyChange("lineDashOffset", oldValue, this.lineDashOffset);
    return this;
  }

  public GeometryStyle setLineGamma(final double gamma) {
    final Object oldValue = this.lineGamma;
    this.lineGamma = gamma;
    firePropertyChange("lineGamma", oldValue, this.lineGamma);
    return this;
  }

  public GeometryStyle setLineGammaMethod(final GammaMethod gammaMethod) {
    final Object oldValue = this.lineGammaMethod;
    this.lineGammaMethod = getWithDefault(gammaMethod, GammaMethod.power);
    firePropertyChange("lineGammaMethod", oldValue, this.lineGammaMethod);
    return this;
  }

  public GeometryStyle setLineJoin(final LineJoin lineJoin) {
    final Object oldValue = this.lineJoin;
    this.lineJoin = lineJoin;
    firePropertyChange("lineJoin", oldValue, this.lineJoin);
    return this;
  }

  public GeometryStyle setLineMiterlimit(final float lineMiterlimit) {
    final Object oldValue = this.lineMiterlimit;
    this.lineMiterlimit = lineMiterlimit;
    firePropertyChange("lineMiterlimit", oldValue, this.lineMiterlimit);
    return this;
  }

  public GeometryStyle setLineOpacity(final double lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 1) {
      throw new IllegalArgumentException("Line opacity must be between 0.0 - 1.0");
    } else {
      setLineOpacity((int)(255 * lineOpacity));
    }
    return this;
  }

  public GeometryStyle setLineOpacity(final int lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 255) {
      throw new IllegalArgumentException("Line opacity must be between 0 - 255");
    } else {
      final Object oldLineOpacity = this.lineOpacity;
      final Object oldLineColor = this.lineColor;
      this.lineOpacity = lineOpacity;
      this.lineColor = WebColors.newAlpha(this.lineColor, this.lineOpacity);
      firePropertyChange("lineOpacity", oldLineOpacity, this.lineOpacity);
      firePropertyChange("lineColor", oldLineColor, this.lineColor);
    }
    return this;
  }

  public GeometryStyle setLineSmooth(final double smooth) {
    final Object oldValue = this.lineSmooth;
    this.lineSmooth = smooth;
    firePropertyChange("lineSmooth", oldValue, this.lineSmooth);
    return this;
  }

  public GeometryStyle setLineWidth(final Quantity<Length> lineWidth) {
    final Object oldValue = this.lineWidth;
    this.lineWidth = getWithDefault(lineWidth, ZERO_PIXEL);
    firePropertyChange("lineWidth", oldValue, this.lineWidth);
    return this;
  }

  public GeometryStyle setPolygonClip(final boolean polygonClip) {
    final Object oldValue = this.polygonClip;
    this.polygonClip = polygonClip;
    firePropertyChange("polygonClip", oldValue, this.polygonClip);
    return this;
  }

  public GeometryStyle setPolygonCompOp(final CompositionOperation polygonCompOp) {
    final Object oldValue = this.polygonCompOp;
    this.polygonCompOp = getWithDefault(polygonCompOp, CompositionOperation.src_over);
    firePropertyChange("polygonCompOp", oldValue, this.polygonCompOp);
    return this;
  }

  public GeometryStyle setPolygonFill(final Color fill) {
    final Object oldPolygonFill = this.polygonFill;
    final Object oldPolygonFillOpacity = this.polygonFillOpacity;
    if (fill == null) {
      this.polygonFill = new Color(128, 128, 128, this.polygonFillOpacity);
    } else {
      this.polygonFill = fill;
      this.polygonFillOpacity = fill.getAlpha();
    }
    firePropertyChange("polygonFill", oldPolygonFill, this.polygonFill);
    firePropertyChange("polygonFillOpacity", oldPolygonFillOpacity, this.polygonFillOpacity);
    return this;
  }

  public GeometryStyle setPolygonFillOpacity(final double polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 1) {
      throw new IllegalArgumentException("Polygon fill opacity must be between 0.0 - 1.0");
    } else {
      setPolygonFillOpacity((int)(255 * polygonFillOpacity));
    }
    return this;
  }

  public GeometryStyle setPolygonFillOpacity(final int polygonFillOpacity) {
    final Object oldPolygonFill = this.polygonFill;
    final Object oldPolygonFillOpacity = this.polygonFillOpacity;
    if (polygonFillOpacity < 0 || polygonFillOpacity > 255) {
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.polygonFillOpacity = polygonFillOpacity;
      this.polygonFill = WebColors.newAlpha(this.polygonFill, this.polygonFillOpacity);
    }
    firePropertyChange("polygonFill", oldPolygonFill, this.polygonFill);
    firePropertyChange("polygonFillOpacity", oldPolygonFillOpacity, this.polygonFillOpacity);
    return this;
  }

  public GeometryStyle setPolygonGamma(final double polygonGamma) {
    final Object oldValue = this.polygonGamma;
    this.polygonGamma = polygonGamma;
    firePropertyChange("polygonGamma", oldValue, this.polygonGamma);
    return this;
  }

  public GeometryStyle setPolygonGammaMethod(final GammaMethod polygonGammaMethod) {
    final Object oldValue = this.polygonGammaMethod;
    this.polygonGammaMethod = getWithDefault(polygonGammaMethod, GammaMethod.power);
    firePropertyChange("polygonGammaMethod", oldValue, this.polygonGammaMethod);
    return this;
  }

  public GeometryStyle setPolygonSmooth(final double polygonSmooth) {
    final Object oldValue = this.polygonSmooth;
    this.polygonSmooth = polygonSmooth;
    firePropertyChange("polygonSmooth", oldValue, this.polygonSmooth);
    return this;
  }
}

package com.revolsys.swing.map.layer.record.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

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

  public static GeometryStyle line(final Color color) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(color);
    return style;
  }

  public static GeometryStyle line(final Color color, final double lineWidth) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(color);
    style.setLineWidth(Measure.valueOf(lineWidth, NonSI.PIXEL));
    return style;
  }

  public static GeometryStyle newStyle() {
    final GeometryStyle style = new GeometryStyle();
    Color color;
    synchronized (COLORS) {
      colorIndex = (colorIndex + 1) % COLORS.size();
      color = COLORS.get(colorIndex);
    }
    style.setLineColor(color);
    style.setPolygonFill(WebColors.setAlpha(color, 127));
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
    style.setLineWidth(Measure.valueOf(lineWidth, NonSI.PIXEL));
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

  private Measure<Length> lineWidth = ONE_PIXEL;

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
    return Collections.unmodifiableList(this.lineDashArray);
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

  public Measure<Length> getLineWidth() {
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

  public void setFillStyle(final Viewport2D viewport, final Graphics2D graphics) {
    graphics.setPaint(this.polygonFill);
    // final Graphic fillPattern = fill.getPattern();
    // if (fillPattern != null) {
    // TODO fillPattern
    // double width = fillPattern.getWidth();
    // double height = fillPattern.getHeight();
    // Rectangle2D.Double patternRect;
    // // TODO units
    // // if (isUseModelUnits()) {
    // // patternRect = new Rectangle2D.Double(0, 0, width
    // // * viewport.getModelUnitsPerViewUnit(), height
    // // * viewport.getModelUnitsPerViewUnit());
    // // } else {
    // patternRect = new Rectangle2D.Double(0, 0, width, height);
    // // }
    // graphics.setPaint(new TexturePaint(fillPattern, patternRect));

    // }
  }

  public void setLineCap(final LineCap lineCap) {
    final Object oldValue = this.lineCap;
    this.lineCap = lineCap;
    firePropertyChange("lineCap", oldValue, this.lineCap);
  }

  public void setLineClip(final boolean lineClip) {
    final Object oldValue = this.lineClip;
    this.lineClip = lineClip;
    firePropertyChange("lineClip", oldValue, this.lineClip);
  }

  public void setLineColor(final Color lineColor) {
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
  }

  public void setLineCompOp(final CompositionOperation lineCompOp) {
    final Object oldValue = this.lineCompOp;
    this.lineCompOp = getWithDefault(lineCompOp, CompositionOperation.src_over);
    firePropertyChange("lineCompOp", oldValue, this.lineCompOp);
  }

  public void setLineDashArray(final List<?> lineDashArray) {
    final Object oldValue = this.lineDashArray;
    this.lineDashArray = new ArrayList<>();
    if (lineDashArray != null) {
      for (final Object dashObject : lineDashArray) {
        if (Property.hasValue(dashObject)) {
          String dashString = dashObject.toString();
          dashString = dashString.replaceAll(" \\[pnt\\]", "");
          final Double dash = MathUtil.toDouble(dashString);
          this.lineDashArray.add(dash);
        }
      }
    }
    firePropertyChange("lineDashArray", oldValue, this.lineDashArray);
  }

  public void setLineDashOffset(final double lineDashOffset) {
    final Object oldValue = this.lineDashOffset;
    this.lineDashOffset = lineDashOffset;
    firePropertyChange("lineDashOffset", oldValue, this.lineDashOffset);
  }

  public void setLineGamma(final double gamma) {
    final Object oldValue = this.lineGamma;
    this.lineGamma = gamma;
    firePropertyChange("lineGamma", oldValue, this.lineGamma);
  }

  public void setLineGammaMethod(final GammaMethod gammaMethod) {
    final Object oldValue = this.lineGammaMethod;
    this.lineGammaMethod = getWithDefault(gammaMethod, GammaMethod.power);
    firePropertyChange("lineGammaMethod", oldValue, this.lineGammaMethod);
  }

  public void setLineJoin(final LineJoin lineJoin) {
    final Object oldValue = this.lineJoin;
    this.lineJoin = lineJoin;
    firePropertyChange("lineJoin", oldValue, this.lineJoin);
  }

  public void setLineMiterlimit(final float lineMiterlimit) {
    final Object oldValue = this.lineMiterlimit;
    this.lineMiterlimit = lineMiterlimit;
    firePropertyChange("lineMiterlimit", oldValue, this.lineMiterlimit);
  }

  public void setLineOpacity(final double lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 1) {
      throw new IllegalArgumentException("Line opacity must be between 0.0 - 1.0");
    } else {
      setLineOpacity((int)(255 * lineOpacity));
    }
  }

  public void setLineOpacity(final int lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 255) {
      throw new IllegalArgumentException("Line opacity must be between 0 - 255");
    } else {
      final Object oldLineOpacity = this.lineOpacity;
      final Object oldLineColor = this.lineColor;
      this.lineOpacity = lineOpacity;
      this.lineColor = WebColors.setAlpha(this.lineColor, this.lineOpacity);
      firePropertyChange("lineOpacity", oldLineOpacity, this.lineOpacity);
      firePropertyChange("lineColor", oldLineColor, this.lineColor);
    }
  }

  public void setLineSmooth(final double smooth) {
    final Object oldValue = this.lineSmooth;
    this.lineSmooth = smooth;
    firePropertyChange("lineSmooth", oldValue, this.lineSmooth);
  }

  public void setLineStyle(final Viewport2D viewport, final Graphics2D graphics) {
    final Color color = getLineColor();
    graphics.setColor(color);
    final Unit<Length> unit = this.lineWidth.getUnit();
    final float width = (float)Viewport2D.toModelValue(viewport, this.lineWidth);

    final float dashOffset = (float)Viewport2D.toModelValue(viewport,
      Measure.valueOf(this.lineDashOffset, unit));

    final float[] dashArray;
    final int dashArraySize = this.lineDashArray.size();
    if (dashArraySize == 0) {
      dashArray = null;
    } else {
      dashArray = new float[dashArraySize];
      for (int i = 0; i < dashArray.length; i++) {
        final Double dashDouble = this.lineDashArray.get(i);
        final float dashFloat = (float)Viewport2D.toModelValue(viewport,
          Measure.valueOf(dashDouble, unit));
        dashArray[i] = dashFloat;
      }
    }

    final int lineCap = this.lineCap.getAwtValue();
    final int lineJoin = this.lineJoin.getAwtValue();
    final BasicStroke basicStroke = new BasicStroke(width, lineCap, lineJoin, this.lineMiterlimit,
      dashArray, dashOffset);
    graphics.setStroke(basicStroke);
  }

  public void setLineWidth(final Measure<Length> lineWidth) {
    final Object oldValue = this.lineWidth;
    this.lineWidth = getWithDefault(lineWidth, ZERO_PIXEL);
    firePropertyChange("lineWidth", oldValue, this.lineWidth);
  }

  public void setPolygonClip(final boolean polygonClip) {
    final Object oldValue = this.polygonClip;
    this.polygonClip = polygonClip;
    firePropertyChange("polygonClip", oldValue, this.polygonClip);
  }

  public void setPolygonCompOp(final CompositionOperation polygonCompOp) {
    final Object oldValue = this.polygonCompOp;
    this.polygonCompOp = getWithDefault(polygonCompOp, CompositionOperation.src_over);
    firePropertyChange("polygonCompOp", oldValue, this.polygonCompOp);
  }

  public void setPolygonFill(final Color fill) {
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
  }

  public void setPolygonFillOpacity(final double polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 1) {
      throw new IllegalArgumentException("Polygon fill opacity must be between 0.0 - 1.0");
    } else {
      setPolygonFillOpacity((int)(255 * polygonFillOpacity));
    }
  }

  public void setPolygonFillOpacity(final int polygonFillOpacity) {
    final Object oldPolygonFill = this.polygonFill;
    final Object oldPolygonFillOpacity = this.polygonFillOpacity;
    if (polygonFillOpacity < 0 || polygonFillOpacity > 255) {
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.polygonFillOpacity = polygonFillOpacity;
      this.polygonFill = WebColors.setAlpha(this.polygonFill, this.polygonFillOpacity);
    }
    firePropertyChange("polygonFill", oldPolygonFill, this.polygonFill);
    firePropertyChange("polygonFillOpacity", oldPolygonFillOpacity, this.polygonFillOpacity);
  }

  public void setPolygonGamma(final double polygonGamma) {
    final Object oldValue = this.polygonGamma;
    this.polygonGamma = polygonGamma;
    firePropertyChange("polygonGamma", oldValue, this.polygonGamma);
  }

  public void setPolygonGammaMethod(final GammaMethod polygonGammaMethod) {
    final Object oldValue = this.polygonGammaMethod;
    this.polygonGammaMethod = getWithDefault(polygonGammaMethod, GammaMethod.power);
    firePropertyChange("polygonGammaMethod", oldValue, this.polygonGammaMethod);
  }

  public void setPolygonSmooth(final double polygonSmooth) {
    final Object oldValue = this.polygonSmooth;
    this.polygonSmooth = polygonSmooth;
    firePropertyChange("polygonSmooth", oldValue, this.polygonSmooth);
  }
}

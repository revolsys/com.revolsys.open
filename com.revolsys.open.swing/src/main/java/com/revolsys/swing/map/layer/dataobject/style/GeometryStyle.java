package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.JavaBeanUtil;

public class GeometryStyle extends MarkerStyle {

  private static int colorIndex = -1;

  public static final List<Color> COLORS = Arrays.asList(WebColors.Maroon,
    WebColors.Olive, WebColors.Green, WebColors.Teal, WebColors.Navy,
    WebColors.Purple, WebColors.Red, WebColors.Yellow, WebColors.Lime,
    WebColors.Aqua, WebColors.Blue, WebColors.Fuchsia);

  public static GeometryStyle createStyle() {
    GeometryStyle style = new GeometryStyle();
    Color color;
    synchronized (COLORS) {
      colorIndex = (colorIndex + 1) % COLORS.size();
      color = COLORS.get(colorIndex);
    }
    style.setLineColor(color);
    style.setPolygonFill(ColorUtil.setAlpha(color, 127));
    return style;
  }

  @Override
  public GeometryStyle clone() {
    return (GeometryStyle)super.clone();
  }

  public static GeometryStyle line(final Color color) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(color);
    return style;
  }

  public static GeometryStyle line(final Color color, final double width) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(color);
    style.setLineWidth(width);
    return style;
  }

  public static GeometryStyle polygon(final Color lineColor,
    final Color fillColor) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(lineColor);
    style.setPolygonFill(fillColor);
    return style;
  }

  public static GeometryStyle polygon(final Color lineColor,
    final int lineWidth, final Color fillColor) {
    final GeometryStyle style = new GeometryStyle();
    style.setLineColor(lineColor);
    style.setLineWidth(lineWidth);
    style.setPolygonFill(fillColor);
    return style;
  }

  private LineCap lineCap = LineCap.BUTT;

  private boolean lineClip = true;

  private Color lineColor = new Color(128, 128, 128, 255);

  private CompositionOperation lineCompositionOperation = CompositionOperation.src_over;

  private double lineGamma = 1.0;

  private GammaMethod lineGammaMethod = GammaMethod.power;

  private LineJoin lineJoin = LineJoin.MITER;

  private float lineMiterlimit = 4;

  private int lineOpacity = 255;

  private double lineSmooth;

  private Measure<Length> lineWidth = ONE_PIXEL;

  private boolean polygonClip = true;

  private CompositionOperation polygonCompositionOperation = CompositionOperation.src_over;

  private Color polygonFill = new Color(128, 128, 128, 255);

  private int polygonFillOpacity = 255;

  private double polygonGamma = 1.0;

  private GammaMethod polygonGammaMethod = GammaMethod.power;

  private double polygonSmooth;

  public GeometryStyle() {
  }

  public GeometryStyle(final Map<String, Object> style) {
    super(style);
    for (final Entry<String, Object> entry : style.entrySet()) {
      final String label = entry.getKey();
      Object value = entry.getValue();
      final GeometryStyleProperty geometryStyleProperty = GeometryStyleProperty.getProperty(label);
      if (geometryStyleProperty != null) {
        final DataType dataType = geometryStyleProperty.getDataType();
        final String propertyName = geometryStyleProperty.getPropertyName();
        value = StringConverterRegistry.toObject(dataType, value);
        JavaBeanUtil.setProperty(this, propertyName, value);
      }
    }
  }

  public LineCap getLineCap() {
    return lineCap;
  }

  public Color getLineColor() {
    return lineColor;
  }

  public CompositionOperation getLineCompositionOperation() {
    return lineCompositionOperation;
  }

  public double getLineGamma() {
    return lineGamma;
  }

  public GammaMethod getLineGammaMethod() {
    return lineGammaMethod;
  }

  public String getLineJoin() {
    return lineJoin.toString();
  }

  public LineJoin getLineJoinEnum() {
    return lineJoin;
  }

  public float getLineMiterlimit() {
    return lineMiterlimit;
  }

  public int getLineOpacity() {
    return lineOpacity;
  }

  public double getLineSmooth() {
    return lineSmooth;
  }

  public double getLineWidth() {
    return lineWidth.doubleValue(NonSI.PIXEL);
  }

  public Measure<Length> getLineWidthMeasure() {
    return lineWidth;
  }

  public CompositionOperation getPolygonCompositionOperation() {
    return polygonCompositionOperation;
  }

  public Color getPolygonFill() {
    return polygonFill;
  }

  public int getPolygonFillOpacity() {
    return polygonFillOpacity;
  }

  public double getPolygonGamma() {
    return polygonGamma;
  }

  public GammaMethod getPolygonGammaMethod() {
    return polygonGammaMethod;
  }

  public double getPolygonSmooth() {
    return polygonSmooth;
  }

  public boolean isLineClip() {
    return lineClip;
  }

  public boolean isPolygonClip() {
    return polygonClip;
  }

  public void setFillStyle(final Viewport2D viewport, final Graphics2D graphics) {
    graphics.setPaint(polygonFill);
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
    this.lineCap = lineCap;
  }

  public void setLineCap(final String lineCap) {
    setLineCap(LineCap.valueOf(lineCap.toUpperCase()));
  }

  public void setLineClip(final boolean lineClip) {
    this.lineClip = lineClip;
  }

  public void setLineColor(final Color lineColor) {
    if (lineColor == null) {
      this.lineColor = new Color(128, 128, 128, lineOpacity);
    } else {
      this.lineColor = lineColor;
      this.lineOpacity = lineColor.getAlpha();
    }
  }

  public void setLineCompositionOperation(
    final CompositionOperation compositionOperation) {
    this.lineCompositionOperation = compositionOperation;
  }

  public void setLineGamma(final double gamma) {
    this.lineGamma = gamma;
  }

  public void setLineGammaMethod(final GammaMethod gammaMethod) {
    this.lineGammaMethod = gammaMethod;
  }

  public void setLineGammaMethod(final String lineGammaMethod) {
    setLineGammaMethod(GammaMethod.valueOf(lineGammaMethod));
  }

  public void setLineJoin(final String lineJoin) {
    setLineJoinEnum(LineJoin.valueOf(lineJoin.toUpperCase()));
  }

  public void setLineJoinEnum(final LineJoin lineJoin) {
    this.lineJoin = lineJoin;
  }

  public void setLineMiterlimit(final float lineMiterlimit) {
    this.lineMiterlimit = lineMiterlimit;
  }

  public void setLineOpacity(final double lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 1) {
      throw new IllegalArgumentException(
        "Line opacity must be between 0.0 - 1.0");
    } else {
      this.lineOpacity = (int)(255 * lineOpacity);
      this.lineColor = getColorWithOpacity(lineColor, this.lineOpacity);
    }
  }

  public void setLineOpacity(final int lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 255) {
      throw new IllegalArgumentException("Line opacity must be between 0 - 255");
    } else {
      this.lineOpacity = lineOpacity;
      this.lineColor = getColorWithOpacity(lineColor, this.lineOpacity);
    }
  }

  public void setLineSmooth(final double smooth) {
    this.lineSmooth = smooth;
  }

  public void setLineStyle(final Viewport2D viewport, final Graphics2D graphics) {
    final Color color = getLineColor();
    graphics.setColor(color);

    final float width = (float)viewport.toDisplayValue(lineWidth);

    final float dashPhase = 0;
    /*
     * TODO final Measure<Length> strokeDashPhase = stroke.getDashOffset(); if
     * (viewport == null) { dashPhase = strokeDashPhase.getValue().floatValue();
     * } else { dashPhase = (float)viewport.toDisplayValue(strokeDashPhase); }
     */
    final float[] dashArray = null;
    /*
     * TODO final List<Measure<Length>> dashes = stroke.getDashArray(); if
     * (dashes == null) { dashArray = null; } else { dashArray = new
     * float[dashes.size()]; for (int i = 0; i < dashArray.length; i++) { final
     * Measure<Length> dash = dashes.get(i); if (viewport == null) {
     * dashArray[i] = dash.getValue().floatValue(); } else { dashArray[i] =
     * (float)viewport.toDisplayValue(dash); } } }
     */
    final int lineCap = this.lineCap.getAwtValue();
    final int lineJoin = this.lineJoin.getAwtValue();
    final BasicStroke basicStroke = new BasicStroke(width, lineCap, lineJoin,
      lineMiterlimit, dashArray, dashPhase);
    graphics.setStroke(basicStroke);
  }

  public void setLineWidth(final double lineWidth) {
    this.lineWidth = Measure.valueOf(lineWidth, NonSI.PIXEL);
  }

  public void setLineWidth(final Measure<Length> lineWidth) {
    this.lineWidth = getWithDefault(lineWidth, ZERO_PIXEL);
  }

  public void setPolygonClip(final boolean polygonClip) {
    this.polygonClip = polygonClip;
  }

  public void setPolygonCompositionOperation(
    final CompositionOperation polygonCompositionOperation) {
    this.polygonCompositionOperation = polygonCompositionOperation;
  }

  public void setPolygonFill(final Color fill) {
    if (fill == null) {
      this.polygonFill = new Color(128, 128, 128, polygonFillOpacity);
    } else {
      this.polygonFill = fill;
      this.polygonFillOpacity = fill.getAlpha();
    }
  }

  public void setPolygonFillOpacity(final double polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 1) {
      throw new IllegalArgumentException(
        "Polygon fill opacity must be between 0.0 - 1.0");
    } else {
      this.polygonFillOpacity = (int)(255 * polygonFillOpacity);
      this.polygonFill = getColorWithOpacity(polygonFill,
        this.polygonFillOpacity);
    }
  }

  public void setPolygonFillOpacity(final int polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 255) {
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.polygonFillOpacity = polygonFillOpacity;
      this.polygonFill = getColorWithOpacity(polygonFill,
        this.polygonFillOpacity);
    }
  }

  public void setPolygonGamma(final double polygonGamma) {
    this.polygonGamma = polygonGamma;
  }

  public void setPolygonGammaMethod(final GammaMethod polygonGammaMethod) {
    this.polygonGammaMethod = polygonGammaMethod;
  }

  public void setPolygonSmooth(final double polygonSmooth) {
    this.polygonSmooth = polygonSmooth;
  }

}

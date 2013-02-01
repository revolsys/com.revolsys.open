package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.JavaBeanUtil;

public class GeometryStyle {

  public static final Measure<Length> TEN_PIXELS = Measure.valueOf(10,
    NonSI.PIXEL);

  public static final Measure<Length> ZERO_PIXEL = Measure.valueOf(0,
    NonSI.PIXEL);

  public static final Measure<Length> ONE_PIXEL = Measure.valueOf(1,
    NonSI.PIXEL);

  public static Color getColorWithOpacity(final Color color, final int opacity) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
  }

  public static <T> T getWithDefault(final T value, final T defaultValue) {
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
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

  public static GeometryStyle marker(final String markerName,
    final int markerSize, final Color lineColor, final int lineWidth,
    final Color fillColor) {
    final GeometryStyle style = new GeometryStyle();
    style.setMarker(new ShapeMarker(markerName));
    style.setMarkerWidth(markerSize);
    style.setMarkerHeight(markerSize);
    style.setMarkerDeltaX(-markerSize / 2);
    style.setMarkerDeltaY(-markerSize / 2);
    style.setLineColor(lineColor);
    style.setPolygonFill(fillColor);
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

  private Measure<Length> markerWidth = TEN_PIXELS;

  private Measure<Length> markerHeight = TEN_PIXELS;

  private Measure<Length> markerDx = ZERO_PIXEL;

  private Measure<Length> markerDy = ZERO_PIXEL;

  private Marker marker = new ShapeMarker("square");

  private boolean lineClip = true;

  private double lineSmooth;

  private double lineGamma = 1.0;

  private GammaMethod lineGammaMethod = GammaMethod.power;

  private CompositionOperation lineCompositionOperation = CompositionOperation.src_over;

  private boolean polygonClip = true;

  private double polygonSmooth;

  private double polygonGamma = 1.0;

  private GammaMethod polygonGammaMethod = GammaMethod.power;

  private CompositionOperation polygonCompositionOperation = CompositionOperation.src_over;

  private Color polygonFill = new Color(128, 128, 128, 255);

  private int lineOpacity = 255;

  private Color lineColor = new Color(128, 128, 128, 255);

  private int polygonFillOpacity = 255;

  private Measure<Length> lineWidth = ONE_PIXEL;

  private float lineMiterlimit = 4;

  private LineCap lineCap = LineCap.BUTT;

  private LineJoin lineJoin = LineJoin.MITER;

  public GeometryStyle() {
  }

  public GeometryStyle(final Map<String, Object> style) {
    for (final Entry<String, Object> entry : style.entrySet()) {
      final String label = entry.getKey();
      Object value = entry.getValue();
      final CartoCssProperty property = CartoCssProperty.getProperty(label);
      if (property != null) {
        final DataType dataType = property.getDataType();
        final String propertyName = property.getPropertyName();
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

  public LineJoin getLineJoin() {
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

  public Marker getMarker() {
    return marker;
  }

  public Measure<Length> getMarkerDx() {
    return markerDx;
  }

  public Measure<Length> getMarkerDy() {
    return markerDy;
  }

  public Measure<Length> getMarkerHeight() {
    return markerHeight;
  }

  public Measure<Length> getMarkerWidth() {
    return markerWidth;
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

  public void setLineJoin(final LineJoin lineJoin) {
    this.lineJoin = lineJoin;
  }

  public void setLineJoin(final String lineJoin) {
    setLineJoin(LineJoin.valueOf(lineJoin.toUpperCase()));
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

  public void setMarker(final Marker marker) {
    this.marker = marker;
  }

  public void setMarkerDeltaX(final double markerDx) {
    setMarkerDx(Measure.valueOf(markerDx, NonSI.PIXEL));
  }

  public void setMarkerDeltaY(final double markerDy) {
    setMarkerDy(Measure.valueOf(markerDy, NonSI.PIXEL));
  }

  public void setMarkerDx(final Measure<Length> markerDx) {
    this.markerDx = getWithDefault(markerDx, ZERO_PIXEL);
  }

  public void setMarkerDy(final Measure<Length> markerDy) {
    this.markerDy = getWithDefault(markerDx, ZERO_PIXEL);
  }

  public void setMarkerHeight(final double markerHeight) {
    setMarkerHeight(Measure.valueOf(markerHeight, NonSI.PIXEL));
  }

  public void setMarkerHeight(final Measure<Length> markerHeight) {
    this.markerHeight = getWithDefault(markerHeight, TEN_PIXELS);
  }

  public void setMarkerWidth(final double markerWidth) {
    setMarkerWidth(Measure.valueOf(markerWidth, NonSI.PIXEL));
  }

  public void setMarkerWidth(final Measure<Length> markerWidth) {
    this.markerWidth = getWithDefault(markerWidth, TEN_PIXELS);
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

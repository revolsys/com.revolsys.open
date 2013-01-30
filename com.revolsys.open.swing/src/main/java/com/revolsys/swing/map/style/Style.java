package com.revolsys.swing.map.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import com.revolsys.swing.map.Viewport2D;

public class Style {

  public static Style line(final Color color) {
    final Style style = new Style();
    style.setLineColor(color);
    return style;
  }

  public static Style line(final Color color, final double width) {
    final Style style = new Style();
    style.setLineColor(color);
    style.setLineWidth(width);
    return style;
  }

  public static Style marker(final String markerName, final int markerSize,
    final Color lineColor, final int lineWidth, final Color fillColor) {
    final Style style = new Style();
    style.setMarker(new ShapeMarker(markerName));
    style.setMarkerWidth(markerSize);
    style.setMarkerHeight(markerSize);
    style.setMarkerDeltaX(-markerSize / 2);
    style.setMarkerDeltaY(-markerSize / 2);
    style.setLineColor(lineColor);
    style.setPolygonFillColor(fillColor);
    return style;
  }

  public static Style polygon(final Color lineColor, final Color fillColor) {
    final Style style = new Style();
    style.setLineColor(lineColor);
    style.setPolygonFillColor(fillColor);
    return style;
  }

  public static Style polygon(final Color lineColor, final int lineWidth,
    final Color fillColor) {
    final Style style = new Style();
    style.setLineColor(lineColor);
    style.setLineWidth(lineWidth);
    style.setPolygonFillColor(fillColor);
    return style;
  }

  private Measure<Length> markerWidth = Measure.valueOf(10, NonSI.PIXEL);

  private Measure<Length> markerHeight = Measure.valueOf(10, NonSI.PIXEL);

  private Measure<Length> markerDeltaX = Measure.valueOf(0, NonSI.PIXEL);

  private Measure<Length> markerDeltaY = Measure.valueOf(0, NonSI.PIXEL);

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

  private Color polygonFillColor = new Color(128, 128, 128, 255);

  private int lineOpacity = 255;

  private Color lineColor = new Color(128, 128, 128, 255);

  private int polygonFillOpacity = 255;

  private Measure<Length> lineWidth = Measure.valueOf(1, NonSI.PIXEL);

  private float lineMiterlimit = 4;

  private LineCap lineCap = LineCap.BUTT;

  private LineJoin lineJoin = LineJoin.MITER;

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

  public Measure<Length> getMarkerDeltaX() {
    return markerDeltaX;
  }

  public Measure<Length> getMarkerDeltaY() {
    return markerDeltaY;
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

  public Color getPolygonFillColor() {
    return polygonFillColor;
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
    graphics.setPaint(polygonFillColor);
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
      this.lineColor = new Color(lineColor.getRed(), lineColor.getGreen(),
        lineColor.getBlue(), this.lineOpacity);
    }
  }

  public void setLineOpacity(final int lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 255) {
      throw new IllegalArgumentException("Line opacity must be between 0 - 255");
    } else {
      this.lineOpacity = lineOpacity;
      this.lineColor = new Color(lineColor.getRed(), lineColor.getGreen(),
        lineColor.getBlue(), this.lineOpacity);
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
    if (lineWidth == null) {
      this.lineWidth = Measure.valueOf(1, NonSI.PIXEL);
    } else {
      this.lineWidth = lineWidth;
    }
  }

  public void setMarker(final Marker marker) {
    this.marker = marker;
  }

  public void setMarkerDeltaX(final Measure<Length> markerDeltaX) {
    if (markerDeltaX == null) {
      this.markerDeltaX = Measure.valueOf(0, NonSI.PIXEL);
    } else {
      this.markerDeltaX = markerDeltaX;
    }
  }

  public void setMarkerDeltaX(final double markerDeltaX) {
    setMarkerDeltaX(Measure.valueOf(markerDeltaX, NonSI.PIXEL));
  }

  public void setMarkerDeltaY(final Measure<Length> markerDeltaY) {
    if (markerDeltaY == null) {
      this.markerDeltaY = Measure.valueOf(0, NonSI.PIXEL);
    } else {
      this.markerDeltaY = markerDeltaY;
    }
  }

  public void setMarkerDeltaY(final double markerDeltaY) {
    setMarkerDeltaY(Measure.valueOf(markerDeltaY, NonSI.PIXEL));
  }

  public void setMarkerHeight(final double markerHeight) {
    setMarkerHeight(Measure.valueOf(markerHeight, NonSI.PIXEL));
  }

  public void setMarkerHeight(final Measure<Length> markerHeight) {
    if (markerHeight == null) {
      this.markerHeight = Measure.valueOf(10, NonSI.PIXEL);
    } else {
      this.markerHeight = markerWidth;
    }
  }

  public void setMarkerWidth(final double markerWidth) {
    setMarkerWidth(Measure.valueOf(markerWidth, NonSI.PIXEL));
  }

  public void setMarkerWidth(final Measure<Length> markerWidth) {
    if (markerWidth == null) {
      this.markerWidth = Measure.valueOf(10, NonSI.PIXEL);
    } else {
      this.markerWidth = markerWidth;
    }
  }

  public void setPolygonClip(final boolean polygonClip) {
    this.polygonClip = polygonClip;
  }

  public void setPolygonCompositionOperation(
    final CompositionOperation polygonCompositionOperation) {
    this.polygonCompositionOperation = polygonCompositionOperation;
  }

  public void setPolygonFillColor(final Color fill) {
    if (fill == null) {
      this.polygonFillColor = new Color(128, 128, 128, polygonFillOpacity);
    } else {
      this.polygonFillColor = fill;
      this.polygonFillOpacity = fill.getAlpha();
    }
  }

  public void setPolygonFillOpacity(final double polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 1) {
      throw new IllegalArgumentException(
        "Polygon fill opacity must be between 0.0 - 1.0");
    } else {
      this.polygonFillOpacity = (int)(255 * polygonFillOpacity);
      this.polygonFillColor = new Color(polygonFillColor.getRed(),
        polygonFillColor.getGreen(), polygonFillColor.getBlue(),
        this.polygonFillOpacity);
    }
  }

  public void setPolygonFillOpacity(final int polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 255) {
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.polygonFillOpacity = polygonFillOpacity;
      this.polygonFillColor = new Color(polygonFillColor.getRed(),
        polygonFillColor.getGreen(), polygonFillColor.getBlue(),
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

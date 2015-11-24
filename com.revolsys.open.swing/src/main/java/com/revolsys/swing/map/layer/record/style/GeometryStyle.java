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
import com.revolsys.datatype.DataTypes;
import com.revolsys.datatype.SimpleDataType;
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
    // addProperty("backgroundColor", Color.class);
    // addProperty("backgroundImage", String.class);
    // addProperty("base", String.class);
    // addProperty("bufferSize", Double.class);
    // addProperty("buildingFill", Color.class);
    // addProperty("buildingFillOpacity", String.class);
    // addProperty("buildingHeight", Double.class);
    // addProperty("compOp", String.class);
    // addProperty("fontDirectory", String.class);
    // addProperty("imageFilters", String.class);
    addProperty("lineCap", new SimpleDataType("lineCap", LineCap.class), LineCap.ROUND);
    addProperty("lineClip", DataTypes.BOOLEAN, true);
    addProperty("lineColor", DataTypes.COLOR, new Color(128, 128, 128));
    final SimpleDataType compositionOperation = new SimpleDataType("compositionOperation",
      CompositionOperation.class);
    addProperty("lineCompOp", compositionOperation, CompositionOperation.src_over);
    addProperty("lineDashOffset", DataTypes.DOUBLE, 0);
    addProperty("lineDashArray", DataTypes.LIST, Collections.emptyList());
    addProperty("lineGamma", DataTypes.DOUBLE, 1.0);
    final SimpleDataType gammaMethod = new SimpleDataType("gammaMethod", GammaMethod.class);
    addProperty("lineGammaMethod", gammaMethod, GammaMethod.power);
    addProperty("lineJoin", new SimpleDataType("lineJoin", LineJoin.class), LineJoin.ROUND);
    addProperty("lineMiterlimit", DataTypes.FLOAT, 4f);
    // addProperty("lineOffset", String.class);
    addProperty("lineOpacity", DataTypes.INT, 255);
    // addProperty("linePattern", String.class);
    // addProperty("linePatternClip", String.class);
    // addProperty("linePatternCompOp", String.class);
    // addProperty("linePatternFile", String.class);
    // addProperty("linePatternSmooth", String.class);
    // addProperty("lineRasterizer", String.class);
    addProperty("lineSmooth", DataTypes.DOUBLE, 0.0);
    addProperty("lineWidth", DataTypes.MEASURE, ONE_PIXEL);
    // addProperty("opacity", String.class);
    // addProperty("point", String.class);
    // addProperty("pointAllowOverlap", String.class);
    // addProperty("pointCompOp", String.class);
    // addProperty("pointFile", String.class);
    // addProperty("pointIgnorePlacement", String.class);
    // addProperty("pointOpacity", String.class);
    // addProperty("pointPlacement", String.class);
    // addProperty("pointTransform", String.class);
    // addProperty("polygon", String.class);
    addProperty("polygonClip", DataTypes.BOOLEAN, true);
    addProperty("polygonCompOp", compositionOperation, CompositionOperation.src_over);
    addProperty("polygonFill", DataTypes.COLOR, new Color(128, 128, 128));
    addProperty("polygonFillOpacity", DataTypes.INT, 255);
    addProperty("polygonGamma", DataTypes.DOUBLE, 1.0);
    addProperty("polygonGammaMethod", gammaMethod, GammaMethod.power);
    // addProperty("polygonPattern", String.class);
    // addProperty("polygonPatternAlignment", String.class);
    // addProperty("polygonPatternClip", String.class);
    // addProperty("polygonPatternCompOp", String.class);
    // addProperty("polygonPatternFile", String.class);
    // addProperty("polygonPatternGamma", String.class);
    // addProperty("polygonPatternOpacity", String.class);
    // addProperty("polygonPatternSmooth", String.class);
    addProperty("polygonSmooth", DataTypes.DOUBLE, 0.0);
    // addProperty("raster", String.class);
    // addProperty("rasterCompOp", String.class);
    // addProperty("rasterFilterFactor", String.class);
    // addProperty("rasterMeshSize", Double.class);
    // addProperty("rasterOpacity", String.class);
    // addProperty("rasterScaling", String.class);
    // addProperty("shield", String.class);
    // addProperty("shieldAllowOverlap", String.class);
    // addProperty("shieldAvoidEdges", String.class);
    // addProperty("shieldCharacterSpacing", String.class);
    // addProperty("shieldClip", String.class);
    // addProperty("shieldCompOp", String.class);
    // addProperty("shieldDx", Double.class);
    // addProperty("shieldDy", Double.class);
    // addProperty("shieldFaceName", String.class);
    // addProperty("shieldFile", String.class);
    // addProperty("shieldFill", Color.class);
    // addProperty("shieldHaloFill", Color.class);
    // addProperty("shieldHaloRadius", String.class);
    // addProperty("shieldHorizontalAlignment", String.class);
    // addProperty("shieldJustifyAlignment", String.class);
    // addProperty("shieldLineSpacing", String.class);
    // addProperty("shieldMinDistance", String.class);
    // addProperty("shieldMinPadding", String.class);
    // addProperty("shieldName", String.class);
    // addProperty("shieldOpacity", String.class);
    // addProperty("shieldPlacement", String.class);
    // addProperty("shieldSize", Double.class);
    // addProperty("shieldSpacing", String.class);
    // addProperty("shieldTextDx", Double.class);
    // addProperty("shieldTextDy", Double.class);
    // addProperty("shieldTextOpacity", String.class);
    // addProperty("shieldTextTransform", String.class);
    // addProperty("shieldverticalAlignment", String.class);
    // addProperty("shieldWrapBefore", String.class);
    // addProperty("shieldWrapCharacter", String.class);
    // addProperty("shieldWrapWidth", Double.class);
    // addProperty("srs", String.class);

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
    setStyle(style);
  }

  @Override
  public GeometryStyle clone() {
    return (GeometryStyle)super.clone();
  }

  public LineCap getLineCap() {
    return this.lineCap;
  }

  public LineCap getLineCapEnum() {
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

  public LineJoin getLineJoinEnum() {
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

  public void setLineCap(final String lineCap) {
    setLineCapEnum(LineCap.valueOf(lineCap.toUpperCase()));
  }

  public void setLineCapEnum(final LineCap lineCap) {
    this.lineCap = lineCap;
  }

  public void setLineClip(final boolean lineClip) {
    this.lineClip = lineClip;
  }

  public void setLineColor(final Color lineColor) {
    if (lineColor == null) {
      this.lineColor = new Color(128, 128, 128, this.lineOpacity);
    } else {
      this.lineColor = lineColor;
      this.lineOpacity = lineColor.getAlpha();
    }
  }

  public void setLineCompOp(final String lineCompOp) {
    if (Property.hasValue(lineCompOp)) {
      this.lineCompOp = CompositionOperation.valueOf(lineCompOp);
    } else {
      this.lineCompOp = CompositionOperation.src_over;
    }
  }

  public void setLineDashArray(final List<?> lineDashArray) {
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
  }

  public void setLineDashOffset(final double lineDashOffset) {
    this.lineDashOffset = lineDashOffset;
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
      throw new IllegalArgumentException("Line opacity must be between 0.0 - 1.0");
    } else {
      this.lineOpacity = (int)(255 * lineOpacity);
      this.lineColor = WebColors.setAlpha(this.lineColor, this.lineOpacity);
    }
  }

  public void setLineOpacity(final int lineOpacity) {
    if (lineOpacity < 0 || lineOpacity > 255) {
      throw new IllegalArgumentException("Line opacity must be between 0 - 255");
    } else {
      this.lineOpacity = lineOpacity;
      this.lineColor = WebColors.setAlpha(this.lineColor, this.lineOpacity);
    }
  }

  public void setLineSmooth(final double smooth) {
    this.lineSmooth = smooth;
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
    this.lineWidth = getWithDefault(lineWidth, ZERO_PIXEL);
  }

  public void setPolygonClip(final boolean polygonClip) {
    this.polygonClip = polygonClip;
  }

  public void setPolygonCompOp(final String polygonCompOp) {
    if (Property.hasValue(polygonCompOp)) {
      this.polygonCompOp = CompositionOperation.valueOf(polygonCompOp);
    } else {
      this.polygonCompOp = CompositionOperation.src_over;
    }
  }

  public void setPolygonFill(final Color fill) {
    if (fill == null) {
      this.polygonFill = new Color(128, 128, 128, this.polygonFillOpacity);
    } else {
      this.polygonFill = fill;
      this.polygonFillOpacity = fill.getAlpha();
    }
  }

  public void setPolygonFillOpacity(final double polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 1) {
      throw new IllegalArgumentException("Polygon fill opacity must be between 0.0 - 1.0");
    } else {
      this.polygonFillOpacity = (int)(255 * polygonFillOpacity);
      this.polygonFill = WebColors.setAlpha(this.polygonFill, this.polygonFillOpacity);
    }
  }

  public void setPolygonFillOpacity(final int polygonFillOpacity) {
    if (polygonFillOpacity < 0 || polygonFillOpacity > 255) {
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.polygonFillOpacity = polygonFillOpacity;
      this.polygonFill = WebColors.setAlpha(this.polygonFill, this.polygonFillOpacity);
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

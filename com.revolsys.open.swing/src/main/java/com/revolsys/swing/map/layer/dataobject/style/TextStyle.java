package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.JavaBeanUtil;

public class TextStyle {

  public static TextStyle text() {
    return new TextStyle();
  }

  /** The orientation of the text in a clockwise direction from the east axis. */
  private double textOrientation = 0;

  private Color textFill = new Color(0, 0, 0);

  private Color textHaloFill = new Color(255, 255, 255);

  private int textOpacity = 255;

  private Measure<Length> textHaloRadiusMeasure = GeometryStyle.ZERO_PIXEL;

  private String textName = "";

  private String textFaceName = "Arial";

  private Measure<Length> textSizeMeasure = GeometryStyle.TEN_PIXELS;

  private String textAlign = "auto";

  private String textVerticalAlignment = "auto";

  private String textOrientationType = "auto";

  private String textPlacementType = "dummy";

  private Measure<Length> textDx = GeometryStyle.ZERO_PIXEL;

  private Measure<Length> textDy = GeometryStyle.ZERO_PIXEL;

  private long lastScale = 0;

  private Font font;

  private Color textBoxColor = new Color(223, 223, 233, 127);

  public TextStyle() {
  }

  public TextStyle(final Map<String, Object> style) {
    for (final Entry<String, Object> entry : style.entrySet()) {
      final String label = entry.getKey();
      Object value = entry.getValue();
      final TextStyleProperty property = TextStyleProperty.getProperty(label);
      if (property != null) {
        final DataType dataType = property.getDataType();
        final String propertyName = property.getPropertyName();
        value = StringConverterRegistry.toObject(dataType, value);
        JavaBeanUtil.setProperty(this, propertyName, value);
      }
    }
  }

  public String getTextAlign() {
    return textAlign;
  }

  public Color getTextBoxColor() {
    return textBoxColor;
  }

  public Measure<Length> getTextDeltaX() {
    return textDx;
  }

  public Measure<Length> getTextDeltaY() {
    return textDy;
  }

  public String getTextFaceName() {
    return textFaceName;
  }

  public Color getTextFill() {
    return textFill;
  }

  public Color getTextHaloFill() {
    return textHaloFill;
  }

  public Measure<Length> getTextHaloRadiusMeasure() {
    return textHaloRadiusMeasure;
  }

  public String getTextName() {
    return textName;
  }

  public int getTextOpacity() {
    return textOpacity;
  }

  public double getTextOrientation() {
    return textOrientation;
  }

  public String getTextOrientationType() {
    return textOrientationType;
  }

  public String getTextPlacementType() {
    return textPlacementType;
  }

  public Measure<Length> getTextSizeMeasure() {
    return textSizeMeasure;
  }

  public String getTextVerticalAlignment() {
    return textVerticalAlignment;
  }

  public void setTextAlign(final String textAlign) {
    if (StringUtils.hasText(textAlign)) {
      this.textAlign = textAlign;
    } else {
      this.textAlign = "auto";
    }
  }

  public void setTextBoxColor(final Color textBoxColor) {
    this.textBoxColor = textBoxColor;
  }

  public void setTextDeltaX(final Measure<Length> textDx) {
    this.textDx = MarkerStyle.getWithDefault(textDx, MarkerStyle.ZERO_PIXEL);
  }

  public void setTextDeltY(final Measure<Length> textDy) {
    this.textDy = MarkerStyle.getWithDefault(textDy, MarkerStyle.ZERO_PIXEL);
  }

  public void setTextDx(final double textDx) {
    setTextDeltaX(Measure.valueOf(textDx, NonSI.PIXEL));
  }

  public void setTextDy(final double textDy) {
    setTextDeltY(Measure.valueOf(textDy, NonSI.PIXEL));
  }

  public void setTextFaceName(final String textFaceName) {
    this.textFaceName = textFaceName;
  }

  public void setTextFill(final Color fill) {
    if (fill == null) {
      this.textFill = new Color(0, 0, 0, textOpacity);
    } else {
      this.textFill = fill;
      this.textOpacity = fill.getAlpha();
    }
  }

  public void setTextHaloFill(final Color fill) {
    if (fill == null) {
      this.textHaloFill = new Color(0, 0, 0, textOpacity);
    } else {
      this.textHaloFill = fill;
    }
  }

  public void setTextHaloRadius(final double textHaloRadius) {
    setTextHaloRadiusMeasure(Measure.valueOf(textHaloRadius, NonSI.PIXEL));
  }

  public void setTextHaloRadiusMeasure(final Measure<Length> textHaloRadius) {
    this.textHaloRadiusMeasure = textHaloRadius;
  }

  public void setTextHorizontalAlignment(final String textHorizontalAlignment) {
    setTextAlign(textHorizontalAlignment);
  }

  public void setTextName(final String textName) {
    if (textName == null) {
      this.textName = "";
    } else {
      this.textName = textName;
    }
  }

  public void setTextOpacity(final int textOpacity) {
    if (textOpacity < 0 || textOpacity > 255) {
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.textOpacity = textOpacity;
      this.textFill = MarkerStyle.getColorWithOpacity(textFill,
        this.textOpacity);
      this.textHaloFill = MarkerStyle.getColorWithOpacity(textHaloFill,
        this.textOpacity);
    }
  }

  public void setTextOrientation(final double textOrientation) {
    this.textOrientation = textOrientation;
  }

  public void setTextOrientationType(final String textOrientationType) {
    this.textOrientationType = textOrientationType;
  }

  public void setTextPlacementType(final String textPlacementType) {
    if (StringUtils.hasText(textPlacementType)) {
      this.textPlacementType = textPlacementType;
    } else {
      this.textPlacementType = "dummy";
    }
  }

  public void setTextSize(final double textSize) {
    setTextSizeMeasure(Measure.valueOf(textSize, NonSI.PIXEL));
  }

  public void setTextSizeMeasure(final Measure<Length> textSize) {
    this.textSizeMeasure = MarkerStyle.getWithDefault(textSize,
      MarkerStyle.TEN_PIXELS);
  }

  public synchronized void setTextStyle(final Viewport2D viewport,
    final Graphics2D graphics) {
    final long scale = (long)viewport.getScale();
    if (font == null || lastScale != scale) {
      lastScale = scale;
      final int style = 0;
      // if (textStyle.getFontWeight() == FontWeight.BOLD) {
      // style += Font.BOLD;
      // }
      // if (textStyle.getFontStyle() == FontStyle.ITALIC) {
      // style += Font.ITALIC;
      // }
      final double fontSize = Viewport2D.toDisplayValue(viewport,
        textSizeMeasure);
      font = new Font(textFaceName, style, (int)Math.ceil(fontSize));
    }
    graphics.setFont(font);
  }

  public void setTextVerticalAlignment(final String textVerticalAlignment) {
    if (StringUtils.hasText(textVerticalAlignment)) {
      this.textVerticalAlignment = textVerticalAlignment;
    } else {
      this.textVerticalAlignment = "auto";
    }
  }
}

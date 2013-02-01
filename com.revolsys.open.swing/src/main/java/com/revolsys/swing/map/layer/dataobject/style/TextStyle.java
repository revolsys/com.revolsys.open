package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.JavaBeanUtil;

public class TextStyle {

  public static TextStyle text() {
    return new TextStyle();
  }

  private double textOrientation = 90;

  private Color textFill = new Color(0, 0, 0);

  private Color textHaloFill = new Color(255, 255, 255);

  private int textOpacity = 255;

  private final Measure<Length> textHaloRadius = GeometryStyle.ZERO_PIXEL;

  private String textFaceName = Font.SANS_SERIF;

  private Measure<Length> textSize = GeometryStyle.TEN_PIXELS;

  public TextStyle() {
  }

  public TextStyle(final Map<String, Object> style) {
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

  public String getTextFaceName() {
    return textFaceName;
  }

  public Color getTextFill() {
    return textFill;
  }

  public Color getTextHaloFill() {
    return textHaloFill;
  }

  public Measure<Length> getTextHaloRadius() {
    return textHaloRadius;
  }

  public int getTextOpacity() {
    return textOpacity;
  }

  public double getTextOrientation() {
    return textOrientation;
  }

  public Measure<Length> getTextSize() {
    return textSize;
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

  public void setTextOpacity(final int textOpacity) {
    if (textOpacity < 0 || textOpacity > 255) {
      throw new IllegalArgumentException("Fill opacity must be between 0 - 255");
    } else {
      this.textOpacity = textOpacity;
      this.textFill = GeometryStyle.getColorWithOpacity(textFill,
        this.textOpacity);
      this.textHaloFill = GeometryStyle.getColorWithOpacity(textHaloFill,
        this.textOpacity);
    }
  }

  public void setTextOrientation(final double textOrientation) {
    this.textOrientation = textOrientation;
  }

  public void setTextSize(final double textSize) {
    setTextSize(Measure.valueOf(textSize, NonSI.PIXEL));
  }

  public void setTextSize(final Measure<Length> textSize) {
    this.textSize = GeometryStyle.getWithDefault(textSize,
      GeometryStyle.TEN_PIXELS);
  }

  public void setTextStyle(final Viewport2D viewport, final Graphics2D graphics) {
    final int style = 0;
    // if (textStyle.getFontWeight() == FontWeight.BOLD) {
    // style += Font.BOLD;
    // }
    // if (textStyle.getFontStyle() == FontStyle.ITALIC) {
    // style += Font.ITALIC;
    // }
    final double fontSize = textSize.doubleValue(SI.METRE);
    final Font font = new Font(textFaceName, style, (int)fontSize);
    graphics.setFont(font);
    graphics.setColor(getTextFill());
  }

}

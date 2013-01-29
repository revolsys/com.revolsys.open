package com.revolsys.swing.map.symbolizer;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import com.revolsys.beans.ExpressionMeasurable;
import com.revolsys.swing.map.layer.dataobject.symbolizer.SymbolizerJexlContext;

public class AbstractTextSymbolizer extends AbstractGeometrySymbolizer {

  public enum FontStyle {
    NORMAL, ITALIC, OBLIQUE
  }

  public enum FontWeight {
    NORMAL, BOLD
  }

  private CharSequence labelPropertyName;

  private CharSequence fontFamily = "sans-serif";

  private FontStyle fontStyle = FontStyle.NORMAL;

  private FontWeight fontWeight = FontWeight.NORMAL;

  private Measure<Length> fontSize = Measure.valueOf(10.0, NonSI.PIXEL);

  private CharSequence color = "#000000";

  private CharSequence outlineColor = "#ffffff";

  private Measure<Length> outlineRadius = Measure.valueOf(5.0, NonSI.PIXEL);

  private final Unit<Length> unit = NonSI.PIXEL;

  public AbstractTextSymbolizer(final CharSequence labelPropertyName) {
    this.labelPropertyName = labelPropertyName;
  }

  public CharSequence getColor() {
    return color;
  }

  public CharSequence getFontFamily() {
    return fontFamily;
  }

  public Measure<Length> getFontSize() {
    return fontSize;
  }

  public FontStyle getFontStyle() {
    return fontStyle;
  }

  public FontWeight getFontWeight() {
    return fontWeight;
  }

  public CharSequence getLabelPropertyName() {
    return labelPropertyName;
  }

  public CharSequence getOutlineColor() {
    return outlineColor;
  }

  public Measure<Length> getOutlineRadius() {
    return outlineRadius;
  }

  public void setColor(final CharSequence color) {
    this.color = color;
  }

  public void setFontFamily(final CharSequence fontFamily) {
    this.fontFamily = fontFamily;
  }

  public void setFontSize(final Measure<Length> fontSize) {
    this.fontSize = fontSize;
    if (fontSize instanceof ExpressionMeasurable) {
      final ExpressionMeasurable<Length> expressionMeasurable = (ExpressionMeasurable<Length>)fontSize;
      expressionMeasurable.setContext(SymbolizerJexlContext.getInstance());

    }
  }

  public void setFontStyle(final FontStyle fontStyle) {
    this.fontStyle = fontStyle;
  }

  public void setFontWeight(final FontWeight fontWeight) {
    this.fontWeight = fontWeight;
  }

  public void setLabelPropertyName(final CharSequence labelPropertyName) {
    this.labelPropertyName = labelPropertyName;
  }

  public void setOutlineColor(final CharSequence outlineColor) {
    this.outlineColor = outlineColor;
  }

  public void setOutlineRadius(final Measure<Length> outlineRadius) {
    this.outlineRadius = outlineRadius;
  }

}

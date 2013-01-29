package com.revolsys.swing.map.symbolizer;

import javax.measure.Measure;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import com.revolsys.beans.ExpressionMeasurable;
import com.revolsys.swing.map.layer.dataobject.symbolizer.SymbolizerJexlContext;

public class TextPointSymbolizer extends AbstractTextSymbolizer {
  private Number anchorX = 0.5;

  private Number anchorY = 0.5;

  private Measure<Length> displacementX = Measure.valueOf(0.0, NonSI.PIXEL);

  private Measure<Length> displacementY = Measure.valueOf(0.0, NonSI.PIXEL);

  /** Clockwise rotation of text in radians. */
  private Measure<Angle> rotation = Measure.valueOf(0.0, SI.RADIAN);

  public TextPointSymbolizer(final CharSequence labelPropertyName) {
    super(labelPropertyName);
  }

  public Number getAnchorX() {
    return anchorX;
  }

  public Number getAnchorY() {
    return anchorY;
  }

  public Measure<Length> getDisplacementX() {
    return displacementX;
  }

  public Measure<Length> getDisplacementY() {
    return displacementY;
  }

  public Measure<Angle> getRotation() {
    return rotation;
  }

  public void setAnchorX(final Number anchorX) {
    this.anchorX = anchorX;
  }

  public void setAnchorY(final Number anchorY) {
    this.anchorY = anchorY;
  }

  public void setDisplacementX(final Measure<Length> displacementX) {
    this.displacementX = displacementX;
  }

  public void setDisplacementY(final Measure<Length> displacementY) {
    this.displacementY = displacementY;
  }

  public void setRotation(final Measure<Angle> rotation) {
    this.rotation = rotation;
    if (rotation instanceof ExpressionMeasurable) {
      final ExpressionMeasurable<Angle> expressionMeasurable = (ExpressionMeasurable<Angle>)rotation;
      expressionMeasurable.setContext(SymbolizerJexlContext.getInstance());

    }
  }

}

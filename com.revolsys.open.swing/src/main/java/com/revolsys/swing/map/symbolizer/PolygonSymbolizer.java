package com.revolsys.swing.map.symbolizer;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

public class PolygonSymbolizer extends AbstractGeometrySymbolizer {

  private Measure<Length> displacementX;

  private Measure<Length> displacementY;

  private Fill fill;

  private Measure<Length> offset;

  private Stroke stroke;

  public PolygonSymbolizer() {
    this(new Stroke(), new Fill(), Measure.valueOf(0.0, NonSI.PIXEL),
      Measure.valueOf(0.0, NonSI.PIXEL), Measure.valueOf(0.0, NonSI.PIXEL));
  }

  public PolygonSymbolizer(final Stroke stroke, final Fill fill,
    final Measure<Length> displacementX, final Measure<Length> displacementY,
    final Measure<Length> offset) {
    setStroke(stroke);
    setFill(fill);
    this.displacementX = displacementX;
    this.displacementY = displacementY;
    this.offset = offset;
  }

  public Measure<Length> getDisplacementX() {
    return displacementX;
  }

  public Measure<Length> getDisplacementY() {
    return displacementY;
  }

  public Fill getFill() {
    return fill;
  }

  public Measure<Length> getOffset() {
    return offset;
  }

  public Stroke getStroke() {
    return stroke;
  }

  public void setDisplacementX(final Measure<Length> displacementX) {
    final Object oldValue = this.displacementX;
    this.displacementX = displacementX;
    getPropertyChangeSupport().firePropertyChange("displacementX", oldValue,
      displacementX);
  }

  public void setDisplacementY(final Measure<Length> displacementY) {
    final Object oldValue = this.displacementY;
    this.displacementY = displacementY;
    getPropertyChangeSupport().firePropertyChange("displacementY", oldValue,
      displacementY);
  }

  public void setFill(final Fill fill) {
    if (fill != this.fill) {
      final Fill oldValue = this.fill;
      if (oldValue != null) {
        oldValue.removePropertyChangeListener(this);
      }

      this.fill = fill;
      if (fill != null) {
        fill.addPropertyChangeListener(this);
      }
      getPropertyChangeSupport().firePropertyChange("fill", oldValue, fill);
    }
  }

  public void setOffset(final Measure<Length> offset) {
    final Object oldValue = this.offset;
    this.offset = offset;
    getPropertyChangeSupport().firePropertyChange("offset", oldValue, offset);
  }

  public void setStroke(final Stroke stroke) {
    if (stroke != this.stroke) {
      final Stroke oldValue = this.stroke;
      if (oldValue != null) {
        oldValue.removePropertyChangeListener(this);
      }

      this.stroke = stroke;
      if (stroke != null) {
        stroke.addPropertyChangeListener(this);
      }
      getPropertyChangeSupport().firePropertyChange("stroke", oldValue, stroke);
    }
  }
}

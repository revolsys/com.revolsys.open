package com.revolsys.swing.map.symbolizer;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

public class LineSymbolizer extends AbstractGeometrySymbolizer {

  private Measure<Length> offset;

  private Stroke stroke;

  private final Unit<Length> unit = NonSI.PIXEL;

  public LineSymbolizer() {
    this(new Stroke());
  }

  public LineSymbolizer(final Stroke stroke) {
    this(stroke, null);
  }

  public LineSymbolizer(final Stroke stroke, final Measure<Length> offset) {
    this.stroke = stroke;
    this.stroke.addPropertyChangeListener(this);
    setOffset(offset);
  }

  public Measure<Length> getOffset() {
    return offset;
  }

  public Stroke getStroke() {
    return stroke;
  }

  public void setOffset(final Measure<Length> offset) {
    final Object oldValue = this.offset;
    if (offset == null) {
      this.offset = Measure.valueOf(0.0, NonSI.PIXEL);
    } else {
      this.offset = offset;
    }
    getPropertyChangeSupport().firePropertyChange("offset", oldValue,
      this.offset);
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

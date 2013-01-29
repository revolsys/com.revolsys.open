package com.revolsys.swing.map.symbolizer;

public class WellKnownMarkGraphicSymbol extends AbstractGraphicSymbol {
  private Fill fill;

  private CharSequence name;

  private Stroke stroke;

  public WellKnownMarkGraphicSymbol() {
    this("square");
  }

  public WellKnownMarkGraphicSymbol(final CharSequence name) {
    this.name = name;
    setStroke(new Stroke());
    setFill(new Fill());
  }

  public Fill getFill() {
    return fill;
  }

  public CharSequence getName() {
    return name;
  }

  public Stroke getStroke() {
    return stroke;
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

  public void setName(final CharSequence name) {
    final Object oldValue = this.name;
    this.name = name;
    getPropertyChangeSupport().firePropertyChange("name", oldValue, name);
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

package com.revolsys.swing.map.symbolizer;

public class PointSymbolizer extends AbstractGeometrySymbolizer {
  private Graphic graphic;

  public PointSymbolizer() {
    this(new Graphic());
  }

  public PointSymbolizer(final Graphic graphic) {
    setGraphic(graphic);
  }

  public Graphic getGraphic() {
    return graphic;
  }

  public void setGraphic(final Graphic graphic) {
    if (graphic != this.graphic) {
      final Graphic oldValue = this.graphic;
      if (oldValue != null) {
        oldValue.removePropertyChangeListener(this);
      }

      this.graphic = graphic;
      if (graphic != null) {
        graphic.addPropertyChangeListener(this);
      }
      getPropertyChangeSupport().firePropertyChange("graphic", oldValue,
        graphic);
    }
  }

}

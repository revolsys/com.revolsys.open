package com.revolsys.swing.map.symbolizer;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

public abstract class AbstractGeometrySymbolizer extends AbstractSymbolizer
  implements GeometrySymbolizer {
  CharSequence geometryPropertyName;

  private Unit<Length> unit = NonSI.PIXEL;

  public AbstractGeometrySymbolizer() {
  }

  public AbstractGeometrySymbolizer(final CharSequence geometryName) {
    this.geometryPropertyName = geometryName;
  }

  @Override
  public CharSequence getGeometryPropertyName() {
    return geometryPropertyName;
  }

  public Unit<Length> getUnit() {
    return unit;
  }

  @Override
  public void setGeometryPropertyName(final CharSequence geometryPropertyName) {
    final Object oldValue = this.geometryPropertyName;
    this.geometryPropertyName = geometryPropertyName;
    propertyChangeSupport.firePropertyChange("geometryPropertyName", oldValue,
      geometryPropertyName);
  }

  public void setUnit(final Unit<Length> unit) {
    final Object oldValue = this.unit;
    this.unit = unit;
    propertyChangeSupport.firePropertyChange("unit", oldValue, unit);
  }

}

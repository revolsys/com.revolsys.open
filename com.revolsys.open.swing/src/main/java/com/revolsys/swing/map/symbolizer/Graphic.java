package com.revolsys.swing.map.symbolizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

public class Graphic implements PropertyChangeListener {
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private Number anchorX = 0.5;

  private Number anchorY = 0.5;

  private Measure<Length> displacementX = Measure.valueOf(0.0, NonSI.PIXEL);

  private Measure<Length> displacementY = Measure.valueOf(0.0, NonSI.PIXEL);

  private Number opacity = 1;

  /** Clockwise rotation of text in radians. */
  private Measure<Angle> rotation = Measure.valueOf(0.0, SI.RADIAN);

  private Measure<Length> size;

  private List<GraphicSymbol> symbols = new ArrayList<GraphicSymbol>();

  public Graphic() {
    this(new WellKnownMarkGraphicSymbol(), Measure.valueOf(6.0, NonSI.PIXEL));
  }

  public Graphic(final CharSequence markName) {
    this(new WellKnownMarkGraphicSymbol(markName), Measure.valueOf(6.0,
      NonSI.PIXEL));
  }

  public Graphic(final GraphicSymbol symbol) {
    this(symbol, Measure.valueOf(32, NonSI.PIXEL));
  }

  public Graphic(final GraphicSymbol symbol, final Measure<Length> size) {
    setSymbols(symbol);
    this.size = size;
  }

  /**
   * Add the property change listener.
   * 
   * @param listener The listener.
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Add the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void addPropertyChangeListener(
    final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
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

  public Number getOpacity() {
    return opacity;
  }

  public Measure<Angle> getRotation() {
    return rotation;
  }

  public Measure<Length> getSize() {
    return size;
  }

  public List<GraphicSymbol> getSymbols() {
    return symbols;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    propertyChangeSupport.firePropertyChange(evt);

  }

  /**
   * Remove the property change listener.
   * 
   * @param listener The listener.
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Remove the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void removePropertyChangeListener(
    final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  public void setAnchorX(final Number anchorX) {
    final Object oldValue = this.anchorX;
    this.anchorX = anchorX;
    propertyChangeSupport.firePropertyChange("anchorX", oldValue, anchorX);
  }

  public void setAnchorY(final Number anchorY) {
    final Object oldValue = this.anchorY;
    this.anchorY = anchorY;
    propertyChangeSupport.firePropertyChange("anchorY", oldValue, anchorY);
  }

  public void setDisplacementX(final Measure<Length> displacementX) {
    final Object oldValue = this.displacementX;
    this.displacementX = displacementX;
    propertyChangeSupport.firePropertyChange("displacementX", oldValue,
      displacementX);
  }

  public void setDisplacementY(final Measure<Length> displacementY) {
    final Object oldValue = this.displacementY;
    this.displacementY = displacementY;
    propertyChangeSupport.firePropertyChange("displacementY", oldValue,
      displacementY);
  }

  public void setOpacity(final Number opacity) {
    final Object oldValue = this.opacity;
    this.opacity = opacity;
    propertyChangeSupport.firePropertyChange("opacity", oldValue, opacity);
  }

  public void setRotation(final Measure<Angle> rotation) {
    final Object oldValue = this.rotation;
    this.rotation = rotation;
    propertyChangeSupport.firePropertyChange("rotation", oldValue, rotation);
  }

  public void setSize(final Measure<Length> size) {
    final Object oldValue = this.size;
    this.size = size;
    propertyChangeSupport.firePropertyChange("size", oldValue, size);
  }

  public void setSymbols(final GraphicSymbol... symbols) {
    setSymbols(Arrays.asList(symbols));
  }

  public void setSymbols(final List<GraphicSymbol> symbols) {
    final List<GraphicSymbol> oldValue = this.symbols;
    for (final GraphicSymbol symbol : oldValue) {
      symbol.removePropertyChangeListener(this);
    }
    this.symbols = new ArrayList<GraphicSymbol>();
    if (symbols != null) {
      for (final GraphicSymbol symbol : symbols) {
        symbol.addPropertyChangeListener(this);
        this.symbols.add(symbol);
      }
    }
    propertyChangeSupport.firePropertyChange("symbols", oldValue, this.symbols);
  }
}

package com.revolsys.swing.map.symbolizer;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Fill {
  private final PropertyChangeSupport eventHandler = new PropertyChangeSupport(
    this);

  private CharSequence colorString;

  private Graphic pattern;

  /** SVG stroke-opacity. */
  private Number opacity = 1;

  public Fill() {
    this("#808080", 1, null);
  }

  public Fill(final CharSequence string) {
    this("#808080", 1, null);
  }

  public Fill(final CharSequence fillColor, final Number opacity,
    final Graphic pattern) {
    this.colorString = fillColor;
    this.opacity = opacity;
    this.pattern = pattern;
  }

  public Fill(final Fill fill) {
    this(fill.getColorString(), fill.getOpacity(), fill.getPattern());
  }

  public Fill(Color color) {
    this.color = color;
  }

  /**
   * Add the property change listener.
   * 
   * @param listener The listener.
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    eventHandler.addPropertyChangeListener(listener);
  }

  /**
   * Add the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    eventHandler.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public Fill clone() {
    return new Fill(this);
  }

  /**
   * Return the opacity as an alpha value in the range 0-255.
   * 
   * @return The alpha.
   */
  public Number getAlpha() {
    return (int)(opacity.floatValue() * 255 + 0.5);
  }

  public CharSequence getColorString() {
    return colorString;
  }

  private Color color;

  public Color getColor() {
    if (color == null) {
      color = CssUtil.getColor(getColorString(), getAlpha());
    }
    return color;
  }

  public Number getOpacity() {
    return opacity;
  }

  public Graphic getPattern() {
    return pattern;
  }

  /**
   * Remove the property change listener.
   * 
   * @param listener The listener.
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    eventHandler.removePropertyChangeListener(listener);
  }

  /**
   * Remove the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    eventHandler.removePropertyChangeListener(propertyName, listener);
  }

  public void setColor(final CharSequence color) {
    final Object oldValue = this.colorString;
    this.colorString = color;
    eventHandler.firePropertyChange("color", oldValue, this.colorString);
  }

  public void setOpacity(final Number opacity) {
    final Object oldValue = this.opacity;
    if (opacity == null || opacity.floatValue() > 1) {
      this.opacity = 1;
    } else if (opacity.floatValue() < 0) {
      this.opacity = 0;
    } else {
      this.opacity = opacity;
    }
    eventHandler.firePropertyChange("opacity", oldValue, this.opacity);
  }

  public void setPattern(final Graphic pattern) {
    final Object oldValue = this.pattern;
    this.pattern = pattern;
    eventHandler.firePropertyChange("pattern", oldValue, this.pattern);
  }
}

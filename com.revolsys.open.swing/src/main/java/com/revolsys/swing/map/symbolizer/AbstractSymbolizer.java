package com.revolsys.swing.map.symbolizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AbstractSymbolizer implements Symbolizer, PropertyChangeListener {

  protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public AbstractSymbolizer() {
    super();
  }

  /**
   * Add the property change listener.
   * 
   * @param listener The listener.
   */
  @Override
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Add the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  @Override
  public void addPropertyChangeListener(
    final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  protected PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
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
  @Override
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Remove the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  @Override
  public void removePropertyChangeListener(
    final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

}

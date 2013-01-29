package com.revolsys.swing.map.symbolizer;

import java.beans.PropertyChangeListener;

public interface Symbolizer {

  /**
   * Add the property change listener.
   * 
   * @param listener The listener.
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Add the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  void addPropertyChangeListener(
    String propertyName,
    final PropertyChangeListener listener);

  /**
   * Remove the property change listener.
   * 
   * @param listener The listener.
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Remove the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  void removePropertyChangeListener(
    String propertyName,
    PropertyChangeListener listener);

}

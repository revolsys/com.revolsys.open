package com.revolsys.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public interface PropertyChangeSupportProxy {
  default void addPropertyChangeListener(final PropertyChangeListener listener) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  default void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  PropertyChangeSupport getPropertyChangeSupport();

  default void removePropertyChangeListener(final PropertyChangeListener listener) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  default void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }
}

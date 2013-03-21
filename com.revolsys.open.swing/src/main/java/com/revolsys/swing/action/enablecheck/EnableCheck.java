package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public interface EnableCheck {
  boolean isEnabled();

  void addPropertyChangeListener(PropertyChangeListener listener);

  void addPropertyChangeListener(String propertyName,
    final PropertyChangeListener listener);

  void removePropertyChangeListener(PropertyChangeListener listener);

  PropertyChangeSupport getPropertyChangeSupport();

  void removePropertyChangeListener(String propertyName,
    PropertyChangeListener listener);
}

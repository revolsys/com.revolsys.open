package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public interface EnableCheck {
  void addPropertyChangeListener(PropertyChangeListener listener);

  void addPropertyChangeListener(String propertyName,
    final PropertyChangeListener listener);

  PropertyChangeSupport getPropertyChangeSupport();

  boolean isEnabled();

  void removePropertyChangeListener(PropertyChangeListener listener);

  void removePropertyChangeListener(String propertyName,
    PropertyChangeListener listener);
}

package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.revolsys.beans.PropertyChangeSupportProxy;

public interface EnableCheck extends PropertyChangeSupportProxy {
  void addListener(PropertyChangeListener listener);

  void addListener(String propertyName, final PropertyChangeListener listener);

  @Override
  PropertyChangeSupport getPropertyChangeSupport();

  boolean isEnabled();

  void removeListener(PropertyChangeListener listener);

  void removeListener(String propertyName, PropertyChangeListener listener);
}

package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractEnableCheck implements EnableCheck,
  PropertyChangeListener {

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  @Override
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public boolean disabled() {
    propertyChangeSupport.firePropertyChange("enabled", true, false);
    return false;
  }

  public boolean enabled() {
    propertyChangeSupport.firePropertyChange("enabled", false, true);
    return true;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    isEnabled();
  }

  @Override
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }
}

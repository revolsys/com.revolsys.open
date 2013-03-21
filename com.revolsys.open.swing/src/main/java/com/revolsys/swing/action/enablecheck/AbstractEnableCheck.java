package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractEnableCheck implements EnableCheck,
  PropertyChangeListener {

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public boolean enabled() {
    propertyChangeSupport.firePropertyChange("enabled", false, true);
    return true;
  }

  public boolean disabled() {
    propertyChangeSupport.firePropertyChange("enabled", true, false);
    return false;
  }

  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    isEnabled();
  }
}

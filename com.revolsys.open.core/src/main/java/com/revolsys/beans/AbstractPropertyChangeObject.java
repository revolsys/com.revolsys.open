package com.revolsys.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AbstractPropertyChangeObject implements PropertyChangeSupportProxy {
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public void addListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  protected void firePropertyChange(final PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }

  protected void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  public void removeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }
}

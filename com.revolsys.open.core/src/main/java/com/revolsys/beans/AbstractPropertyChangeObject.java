package com.revolsys.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.revolsys.util.ExceptionUtil;

public class AbstractPropertyChangeObject implements
  PropertyChangeSupportProxy, Cloneable {
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public void addListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  protected AbstractPropertyChangeObject clone() {
    try {
      final AbstractPropertyChangeObject clone = (AbstractPropertyChangeObject)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (final CloneNotSupportedException e) {
      ExceptionUtil.throwUncheckedException(e);
      return null;
    }
  }

  protected void firePropertyChange(final PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }

  protected void firePropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    propertyChangeSupport.fireIndexedPropertyChange(propertyName, index,
      oldValue, newValue);
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

package com.revolsys.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;

public class AbstractPropertyChangeObject implements PropertyChangeSupportProxy, Cloneable {
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public void addListener(final PropertyChangeListener listener) {
    Property.addListener(this, listener);
  }

  public void addListener(final String propertyName, final PropertyChangeListener listener) {
    Property.addListener(this, propertyName, listener);
  }

  @Override
  protected AbstractPropertyChangeObject clone() {
    try {
      final AbstractPropertyChangeObject clone = (AbstractPropertyChangeObject)super.clone();
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      return clone;
    } catch (final CloneNotSupportedException e) {
      Exceptions.throwUncheckedException(e);
      return null;
    }
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public void removeListener(final PropertyChangeListener listener) {
    Property.removeListener(this, listener);
  }

  public void removeListener(final String propertyName, final PropertyChangeListener listener) {
    Property.removeListener(this, propertyName, listener);
  }
}

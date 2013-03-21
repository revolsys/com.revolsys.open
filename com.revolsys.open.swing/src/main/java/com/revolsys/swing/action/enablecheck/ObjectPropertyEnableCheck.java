package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeSupport;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.JavaBeanUtil;

public class ObjectPropertyEnableCheck extends AbstractEnableCheck {
  private Object object;

  private String propertyName;

  private Object value;

  public ObjectPropertyEnableCheck(Object object, String propertyName) {
    this(object, propertyName, true);
  }

  public ObjectPropertyEnableCheck(Object object, String propertyName,
    Object value) {
    this.object = object;
    if (object instanceof PropertyChangeSupportProxy) {
      PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)object;
      PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
      // TODO how to make this a weak reference
      propertyChangeSupport.addPropertyChangeListener(propertyName, this);
    }
    this.propertyName = propertyName;
    this.value = value;
  }

  @Override
  public boolean isEnabled() {
    Object value = JavaBeanUtil.getValue(object, propertyName);
    if (EqualsRegistry.equal(value, this.value)) {
      return enabled();
    } else {
      return disabled();
    }
  }

  @Override
  public String toString() {
    return object + "." + propertyName + "=" + value;
  }
}

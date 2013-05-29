package com.revolsys.swing.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.util.JavaBeanUtil;

/**
 * A property change listener that uses the propertyName and newValue from the {@link PropertyChangeEvent}
 * to set the value for that property on the object.
 *
 */
public class BeanPropertyListener implements PropertyChangeListener {

  private Reference<Object> reference;

  public BeanPropertyListener(Object object) {
    reference = new WeakReference<Object>(object);
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    Object object = reference.get();
    if (object != null) {
      String propertyName = event.getPropertyName();
      Object value = event.getNewValue();
      JavaBeanUtil.setProperty(object, propertyName, value);
    }
  }

}

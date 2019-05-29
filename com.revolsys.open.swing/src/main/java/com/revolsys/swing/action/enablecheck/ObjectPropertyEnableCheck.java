package com.revolsys.swing.action.enablecheck;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.jeometry.common.data.type.DataType;

import com.revolsys.util.Property;

public class ObjectPropertyEnableCheck extends AbstractEnableCheck {
  private boolean inverse = false;

  private final Reference<Object> object;

  private final String propertyName;

  private final Object value;

  public ObjectPropertyEnableCheck(final Object object, final String propertyName) {
    this(object, propertyName, true);
  }

  public ObjectPropertyEnableCheck(final Object object, final String propertyName,
    final Object value) {
    this(object, propertyName, value, false);
  }

  public ObjectPropertyEnableCheck(final Object object, final String propertyName,
    final Object value, final boolean inverse) {
    this.object = new WeakReference<>(object);
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
    Property.addListener(object, propertyName, this);
  }

  @Override
  public boolean isEnabled() {
    final Object value = Property.get(this.object.get(), this.propertyName);
    final boolean equal = DataType.equal(value, this.value);
    if (equal == !this.inverse) {
      return enabled();
    } else {
      return disabled();
    }
  }

  @Override
  public String toString() {
    return this.object.get() + "." + this.propertyName + "=" + this.value;
  }
}

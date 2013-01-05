package com.revolsys.swing.field;

import org.apache.commons.beanutils.MethodUtils;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

public class InvokeMethodStringConverter extends ObjectToStringConverter {

  private Object object;

  private String methodName;

  public InvokeMethodStringConverter(Object object, String methodName) {
    if (object == null) {
      throw new IllegalArgumentException("Object cannot be null " + this);
    }
    this.object = object;
    this.methodName = methodName;
  }

  @Override
  public String getPreferredStringForItem(Object item) {
    if (item == null) {
      return "";
    } else {
      try {
        if (object instanceof Class<?>) {
          final Class<?> clazz = (Class<?>)object;
          return (String)MethodUtils.invokeStaticMethod(clazz, methodName, new Object[] {item});
        } else {
          return (String)MethodUtils.invokeMethod(object, methodName, new Object[] {item});
        }
      } catch (final Throwable e) {
        throw new RuntimeException("Unable to invoke " + this, e);
      }
    }
  }

}

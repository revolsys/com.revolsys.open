package com.revolsys.filter;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodFilter<T> implements Filter<T> {

  /** The object to invoke the method on. */
  private Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final String methodName;

  public InvokeMethodFilter(final Object object, final String methodName,
    final Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  public InvokeMethodFilter(final String methodName, final Object... parameters) {
    this(null, methodName, parameters);
  }

  @Override
  public boolean accept(final T item) {
    Object result;
    try {
      Object object = this.object;
      Object[] parameters = this.parameters;
      if (object == null) {
        if (item == null) {
          return true;
        }
        object = item;
      } else {
        parameters = new Object[parameters.length + 1];
        System.arraycopy(parameters, 0, this.parameters, 0, this.parameters.length);
        parameters[parameters.length - 1] = item;
      }
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        result = MethodUtils.invokeStaticMethod(clazz, this.methodName, parameters);
      } else {
        result = MethodUtils.invokeMethod(object, this.methodName, parameters);
      }

    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
      return true;
    }
    return Boolean.TRUE.equals(result);
  }

  @Override
  public String toString() {
    if (this.object == null) {
      return this.methodName + this.parameters;
    } else {
      return this.object.getClass() + "." + this.methodName + this.parameters;
    }
  }
}

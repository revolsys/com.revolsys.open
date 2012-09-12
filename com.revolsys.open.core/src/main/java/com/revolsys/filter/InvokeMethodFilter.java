package com.revolsys.filter;

import java.lang.reflect.Method;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodFilter<T> implements Filter<T> {

  /** A constant for zero length parameters. */
  public static final Object[] PARAMETERS = new Object[0];

  /** The object to invoke the method on. */
  private final Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final String methodName;

  private Method method;

  public InvokeMethodFilter(final Class clazz, final String methodName,
    final Object... parameters) {
    this.object = clazz;
    this.methodName = methodName;
    for (final Method method : clazz.getMethods()) {
      if (method.getName().equals(methodName)) {
        this.method = method;
      }
    }
    if (this.method == null) {
      throw new IllegalArgumentException("Method does not exist");
    }
    this.parameters = new Object[parameters.length + 1];
    System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
  }

  public InvokeMethodFilter(final Object object, final String methodName,
    final Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    for (final Method method : object.getClass().getMethods()) {
      if (method.getName().equals(methodName)) {
        this.method = method;
      }
    }
    if (this.method == null) {
      throw new IllegalArgumentException("Method does not exist");
    }
    this.parameters = new Object[parameters.length + 1];
    System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
  }

  @Override
  public boolean accept(final T item) {
    try {
      parameters[parameters.length - 1] = item;
      return (Boolean)method.invoke(object, parameters);
    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
      return false;
    }
  }

  @Override
  public String toString() {
    return object.getClass() + "." + methodName + parameters;
  }
}

package com.revolsys.beans;

import java.lang.reflect.Method;

import com.revolsys.util.ExceptionUtil;

public class MethodInvoker {

  /** The object to invoke the method on. */
  private final Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final Method method;

  /**
   * Construct a new MethodInvoker.
   * 
   * @param object The object to invoke the method on.
   * @param method The method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public MethodInvoker(final Method method, final Object object,
    final Object... parameters) {
    this.object = object;
    this.method = method;
    this.parameters = parameters;
  }

  @SuppressWarnings("unchecked")
  public <T> T invoke() {
    try {
      return (T)method.invoke(object, parameters);
    } catch (final Throwable e) {
      return (T)ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public String toString() {
    if (object instanceof Class) {
      return object + " " + method;
    } else {
      return object.getClass() + " " + method;
    }
  }
}

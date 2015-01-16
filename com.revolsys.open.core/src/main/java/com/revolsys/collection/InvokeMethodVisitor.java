package com.revolsys.collection;

import java.lang.reflect.Method;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodVisitor<T> implements Visitor<T> {

  /** A constant for zero length parameters. */
  public static final Object[] PARAMETERS = new Object[0];

  /** The object to invoke the method on. */
  private final Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final String methodName;

  private Method method;

  public InvokeMethodVisitor(final Class<?> clazz, final String methodName,
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

  public InvokeMethodVisitor(final Object object, final String methodName,
    final Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    this.parameters = new Object[parameters.length + 1];
    System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
    for (final Method method : object.getClass().getMethods()) {
      final String name = method.getName();
      if (name.equals(methodName)
          && method.getParameterTypes().length == this.parameters.length) {
        this.method = method;
      }
    }
    if (this.method == null) {
      throw new IllegalArgumentException("Method does not exist");
    }
  }

  @Override
  public String toString() {
    return this.object.getClass() + "." + this.methodName + this.parameters;
  }

  @Override
  public boolean visit(final T item) {
    try {
      this.parameters[this.parameters.length - 1] = item;
      return (Boolean)this.method.invoke(this.object, this.parameters);
    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
      return false;
    }
  }
}

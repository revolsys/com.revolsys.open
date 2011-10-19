package com.revolsys.collection;

import java.lang.reflect.Method;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodVisitor<T> implements Visitor<T> {

  /** A constant for zero length parameters. */
  public static final Object[] PARAMETERS = new Object[0];

  /** The object to invoke the method on. */
  private Object object;

  /** The parameters to pass to the method. */
  private Object[] parameters;

  /** The name of the method to invoke. */
  private String methodName;

  private Method method;

  public InvokeMethodVisitor(final Object object, final String methodName,
    Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    for (Method method : object.getClass().getMethods()) {
      if (method.getName().equals(methodName)) {
        this.method = method;
      }
    }
    if (this.method == null) {
      throw new IllegalArgumentException("Method does not exist");
    }
    this.parameters = new Object[parameters.length +1];
    System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
  }
  public InvokeMethodVisitor(final Class clazz, final String methodName,
    Object... parameters) {
    this.object = clazz;
    this.methodName = methodName;
    for (Method method : clazz.getMethods()) {
      if (method.getName().equals(methodName)) {
        this.method = method;
      }
    }
    if (this.method == null) {
      throw new IllegalArgumentException("Method does not exist");
    }
    this.parameters = new Object[parameters.length +1];
    System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
  }

  @Override
  public String toString() {
    return object.getClass() + "." + methodName + parameters;
  }

  public boolean visit(T item) {
    try {
      parameters[parameters.length-1] = item;
      return (Boolean)method.invoke(object, parameters);
    } catch (Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
      return false;
    }
  }
}

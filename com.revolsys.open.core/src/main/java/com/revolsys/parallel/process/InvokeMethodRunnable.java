package com.revolsys.parallel.process;

import java.util.Arrays;

import org.apache.commons.beanutils.MethodUtils;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class InvokeMethodRunnable implements Runnable {
  /** A constant for zero length parameters. */
  public static final Object[] NULL_PARAMETERS = new Object[0];

  /** The object to invoke the method on. */
  private final Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final String methodName;

  /**
   * Construct a new InvokeMethodRunnable with no parameters.
   * 
   * @param object The object to invoke the method on.
   * @param methodName The name of the method to invoke.
   */
  public InvokeMethodRunnable(final Object object, final String methodName) {
    this(object, methodName, NULL_PARAMETERS);
  }

  /**
   * Construct a new InvokeMethodRunnable.
   * 
   * @param object The object to invoke the method on.
   * @param methodName The name of the method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public InvokeMethodRunnable(final Object object, final String methodName,
    final Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  /**
   * Execute the method.
   */
  public void run() {
    try {
      if (object == null) {
        throw new RuntimeException("Object cannot be null " + this);
      } else if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        MethodUtils.invokeStaticMethod(clazz, methodName, parameters);
      } else {
        MethodUtils.invokeMethod(object, methodName, parameters);
      }
    } catch (Throwable e) {
      throw new RuntimeException("Unable to invoke " + this, e);
    }
  }

  @Override
  public String toString() {
    if (object == null) {
      return object + "." + methodName + parameters;
    } else if (object instanceof Class<?>) {
      return object + "." + methodName + parameters;
    } else {
      return object.getClass() + "." + methodName + Arrays.toString(parameters);
    }
  }
}

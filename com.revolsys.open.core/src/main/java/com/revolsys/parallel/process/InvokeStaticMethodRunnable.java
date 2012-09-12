package com.revolsys.parallel.process;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class InvokeStaticMethodRunnable implements Runnable {
  /** A constant for zero length parameters. */
  public static final Object[] NULL_PARAMETERS = new Object[0];

  /** The object to invoke the method on. */
  private final Class<?> clazz;

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
  public InvokeStaticMethodRunnable(final Class<?> clazz,
    final String methodName) {
    this(clazz, methodName, NULL_PARAMETERS);
  }

  /**
   * Construct a new InvokeMethodRunnable.
   * 
   * @param object The object to invoke the method on.
   * @param methodName The name of the method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public InvokeStaticMethodRunnable(final Class<?> clazz,
    final String methodName, final Object... parameters) {
    this.clazz = clazz;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  /**
   * Execute the method.
   */
  @Override
  public void run() {
    try {
      MethodUtils.invokeStaticMethod(clazz, methodName, parameters);
    } catch (final Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public String toString() {
    return clazz + "." + methodName + parameters;
  }
}

package com.revolsys.parallel.process;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

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
  private Object object;

  /** The parameters to pass to the method. */
  private Object[] parameters;

  /** The name of the method to invoke. */
  private String methodName;

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
      MethodUtils.invokeMethod(object, methodName, parameters);
    } catch (Throwable e) {
      ExceptionUtil.throwUncheckedException(e);
    }
  }
}

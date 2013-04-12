package com.revolsys.beans;

import java.lang.reflect.Method;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class MethodRunnable extends MethodInvoker implements Runnable {
  /**
   * Construct a new InvokeMethodCallable.
   * 
   * @param object The object to invoke the method on.
   * @param method The method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public MethodRunnable(final Method method, final Object object,
    final Object... parameters) {
    super(method, object, parameters);
  }

  /**
   * Execute the method.
   */
  @Override
  public void run() {
    invoke();
  }

}

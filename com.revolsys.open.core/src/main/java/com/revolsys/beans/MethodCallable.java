package com.revolsys.beans;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class MethodCallable<T> extends MethodInvoker implements Callable<T> {
  /**
   * Construct a new InvokeMethodCallable.
   * 
   * @param object The object to invoke the method on.
   * @param method The method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public MethodCallable(final Method method, final Object object,
    final Object... parameters) {
    super(method, object, parameters);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T call() throws Exception {
    return (T)invoke();
  }
}

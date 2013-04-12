package com.revolsys.beans;

import java.util.concurrent.Callable;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class InvokeMethodCallable<T> implements Callable<T> {

  public static <V> V invokeAndWait(final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodCallable<V> callable = new InvokeMethodCallable<V>(
      object, methodName, parameters);
    return RunnableCallable.invokeAndWait(callable);
  }

  /** The object to invoke the method on. */
  private final Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final String methodName;

  /**
   * Construct a new InvokeMethodCallable.
   * 
   * @param object The object to invoke the method on.
   * @param methodName The name of the method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public InvokeMethodCallable(final Object object, final String methodName,
    final Object... parameters) {
    this.object = object;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  /**
   * Execute the method.
   */
  @Override
  @SuppressWarnings("unchecked")
  public T call() throws Exception {
    try {
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        return (T)MethodUtils.invokeStaticMethod(clazz, methodName, parameters);
      } else {
        return (T)MethodUtils.invokeMethod(object, methodName, parameters);
      }
    } catch (final Throwable e) {
      return (T)ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public String toString() {
    return object.getClass() + "." + methodName + parameters;
  }
}

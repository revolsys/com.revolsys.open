package com.revolsys.parallel.process;

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
  public InvokeMethodCallable(final Object object, final String methodName) {
    this(object, methodName, NULL_PARAMETERS);
  }

  /**
   * Construct a new InvokeMethodRunnable.
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

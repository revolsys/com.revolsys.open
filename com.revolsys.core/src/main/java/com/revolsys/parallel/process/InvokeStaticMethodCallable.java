package com.revolsys.parallel.process;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class InvokeStaticMethodCallable<T> implements Callable<T> {
  /** A constant for zero length parameters. */
  public static final Object[] NULL_PARAMETERS = new Object[0];

  /** The object to invoke the method on. */
  private Class<?> clazz;

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
  public InvokeStaticMethodCallable(final Class<?> clazz,
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
  public InvokeStaticMethodCallable(final Class<?> clazz,
    final String methodName, final Object... parameters) {
    this.clazz = clazz;
    this.methodName = methodName;
    this.parameters = parameters;
  }

  /**
   * Execute the method.
   */
  public void run() {
  }

  public T call() throws Exception {
    try {
      return (T)MethodUtils.invokeStaticMethod(clazz, methodName, parameters);
    } catch (InvocationTargetException e) {
      ExceptionUtil.throwCauseException(e);
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    }
    return null;
  }

  @Override
  public String toString() {
    return clazz + "." + methodName + parameters;
  }
}

package com.revolsys.parallel.process;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.LoggerFactory;

import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class InvokeMethodRunnable implements Runnable {

  public static void invokeAndWait(final Object object,
    final String methodName, final Object... parameters) {
    final InvokeMethodRunnable runnable = new InvokeMethodRunnable(object,
      methodName, parameters);
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(runnable);
      } catch (final InterruptedException e) {
        ExceptionUtil.throwUncheckedException(e);
      } catch (final InvocationTargetException e) {
        ExceptionUtil.throwCauseException(e);
      }
    }
  }

  public static void invokeLater(final Object object, final String methodName,
    final Object... parameters) {
    final InvokeMethodRunnable runnable = new InvokeMethodRunnable(object,
      methodName, parameters);
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public static void run(final Object object, final String methodName,
    final List<Object> parameters) {
    final Object[] parameterArray = parameters.toArray();
    run(object, methodName, parameterArray);
  }

  public static void run(final Object object, final String methodName,
    final Object... parameterArray) {
    try {
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        MethodUtils.invokeStaticMethod(clazz, methodName, parameterArray);
      } else {
        MethodUtils.invokeMethod(object, methodName, parameterArray);
      }
    } catch (final InvocationTargetException e) {
      ExceptionUtil.throwCauseException(e);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to invoke "
        + toString(object, methodName, parameterArray), e);
    }
  }

  public static String toString(final Object object, final String methodName,
    final List<Object> parameters) {
    Class<?> clazz;
    if (object instanceof Class) {
      clazz = (Class<?>)object;
    } else {
      clazz = object.getClass();
    }
    return clazz.getName() + "." + methodName + "("
      + CollectionUtil.toString(parameters) + ")";
  }

  public static String toString(final Object object, final String methodName,
    final Object... parameters) {
    return toString(object, methodName, Arrays.asList(parameters));
  }

  /** The object to invoke the method on. */
  private Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final String methodName;

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
   * Construct a new InvokeMethodRunnable.
   * 
   * @param object The object to invoke the method on.
   * @param methodName The name of the method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  protected InvokeMethodRunnable(final String methodName,
    final Object... parameters) {
    this.methodName = methodName;
    this.parameters = parameters;
  }

  public Object getObject() {
    return object;
  }

  /**
   * Execute the method.
   */
  @Override
  public void run() {
    final Object object = getObject();
    if (object == null) {
      LoggerFactory.getLogger(getClass())
        .debug("Object cannot be null " + this);
    } else {
      run(object, methodName, parameters);
    }
  }

  protected void setObject(final Object object) {
    this.object = object;
  }

  @Override
  public String toString() {
    if (object == null) {
      return methodName + parameters;
    } else if (object instanceof Class<?>) {
      return object + "." + methodName + "(" + Arrays.toString(parameters)
        + ")";
    } else {
      return object.getClass() + "." + methodName + "("
        + Arrays.toString(parameters) + ")";
    }
  }
}

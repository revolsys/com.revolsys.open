package com.revolsys.parallel.process;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.LoggerFactory;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class InvokeMethodRunnable implements Runnable {

  public static void invokeLater(final Object object, final String methodName,
    final Object... parameters) {
    final InvokeMethodRunnable runnable = new InvokeMethodRunnable(object,
      methodName, parameters);
    SwingUtilities.invokeLater(runnable);
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
    try {
      final Object object = getObject();
      if (object == null) {
        LoggerFactory.getLogger(getClass()).debug(
          "Object cannot be null " + this);
      } else if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        MethodUtils.invokeStaticMethod(clazz, methodName, parameters);
      } else {
        MethodUtils.invokeMethod(object, methodName, parameters);
      }
    } catch (final InvocationTargetException e) {
      throw new RuntimeException("Unable to invoke " + this, e.getCause());
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to invoke " + this, e);
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
      return object + "." + methodName + parameters;
    } else {
      return object.getClass() + "." + methodName + Arrays.toString(parameters);
    }
  }
}

package com.revolsys.parallel.process;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.LoggerFactory;

import com.revolsys.parallel.AbstractRunnable;
import com.revolsys.util.ExceptionUtil;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 * 
 * @author Paul Austin
 */
public class InvokeMethodRunnable extends AbstractRunnable {

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
    final StringBuffer string = new StringBuffer();

    if (object == null) {
    } else if (object instanceof Class<?>) {
      string.append(object);
      string.append('.');
    } else {
      string.append(object.getClass());
      string.append('.');
    }
    string.append(methodName);
    string.append('(');
    for (int i = 0; i < parameters.size(); i++) {
      if (i > 0) {
        string.append(',');
      }
      final Object parameter = parameters.get(i);
      if (parameter == null) {
        string.append("null");
      } else {
        string.append(parameter.getClass());
      }
    }
    string.append(')');
    string.append('\n');
    string.append(parameters);

    return string.toString();
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

  /**
   * Execute the method.
   */
  @Override
  public void doRun() {
    final Object object = getObject();
    if (object == null) {
      LoggerFactory.getLogger(getClass())
        .debug("Object cannot be null " + this);
    } else {
      run(object, methodName, parameters);
    }
  }

  public Object getObject() {
    return object;
  }

  protected void setObject(final Object object) {
    this.object = object;
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();

    if (object == null) {
    } else if (object instanceof Class<?>) {
      string.append(object);
      string.append('.');
    } else {
      string.append(object.getClass());
      string.append('.');
    }
    string.append(methodName);
    string.append('(');
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        string.append(',');
      }
      final Object parameter = parameters[i];
      if (parameter == null) {
        string.append("null");
      } else {
        string.append(parameter.getClass());
      }
    }
    string.append(')');
    string.append('\n');
    string.append(Arrays.toString(parameters));

    return string.toString();
  }
}

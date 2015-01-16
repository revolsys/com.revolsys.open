package com.revolsys.parallel.process;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.revolsys.parallel.AbstractRunnable;
import com.revolsys.util.Property;

/**
 * A runnable class which will invoke a method on an object with the specified
 * parameters.
 *
 * @author Paul Austin
 */
public class InvokeMethodRunnable extends AbstractRunnable implements Process {

  public static void run(final Object object, final String methodName,
    final List<Object> parameters) {
    final Object[] parameterArray = parameters.toArray();
    Property.invoke(object, methodName, parameterArray);
  }

  /** The object to invoke the method on. */
  private Reference<Object> object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final String methodName;

  private String beanName;

  private ProcessNetwork processNetwork;

  /**
   * Construct a new InvokeMethodRunnable.
   *
   * @param object The object to invoke the method on.
   * @param methodName The name of the method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public InvokeMethodRunnable(final Object object, final String methodName,
    final Object... parameters) {
    this.object = new WeakReference<Object>(object);
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
      Property.invoke(object, this.methodName, this.parameters);
    }
  }

  @Override
  public String getBeanName() {
    return this.beanName;
  }

  public Object getObject() {
    return this.object.get();
  }

  @Override
  public ProcessNetwork getProcessNetwork() {
    return this.processNetwork;
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  protected void setObject(final Object object) {
    this.object = new WeakReference<Object>(object);
  }

  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder();
    final Object object = getObject();
    if (object == null) {
    } else if (object instanceof Class<?>) {
      string.append(object);
      string.append('.');
    } else {
      string.append(object.getClass());
      string.append('.');
    }
    string.append(this.methodName);
    string.append('(');
    for (int i = 0; i < this.parameters.length; i++) {
      if (i > 0) {
        string.append(',');
      }
      final Object parameter = this.parameters[i];
      if (parameter == null) {
        string.append("null");
      } else {
        string.append(parameter.getClass());
      }
    }
    string.append(')');
    string.append('\n');
    string.append(Arrays.toString(this.parameters));

    return string.toString();
  }

}

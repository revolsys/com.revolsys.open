package com.revolsys.beans;

import java.lang.reflect.Method;

import com.revolsys.parallel.AbstractRunnable;
import com.revolsys.util.JavaBeanUtil;

public class MethodInvoker extends AbstractRunnable {

  /** The object to invoke the method on. */
  private final Object object;

  /** The parameters to pass to the method. */
  private final Object[] parameters;

  /** The name of the method to invoke. */
  private final Method method;

  /**
   * Construct a new MethodInvoker.
   *
   * @param object The object to invoke the method on.
   * @param method The method to invoke.
   * @param parameters The parameters to pass to the method.
   */
  public MethodInvoker(final Method method, final Object object,
    final Object... parameters) {
    this.object = object;
    this.method = method;
    this.parameters = parameters;
  }

  @Override
  public void doRun() {
    invoke();
  }

  @SuppressWarnings("unchecked")
  public <T> T invoke() {
    return (T)JavaBeanUtil.method(this.method, this.object, this.parameters);
  }

  @Override
  public String toString() {
    if (this.object instanceof Class) {
      return this.object + " " + this.method;
    } else {
      return this.object.getClass() + " " + this.method;
    }
  }
}

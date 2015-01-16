package com.revolsys.swing.component;

import java.awt.Component;
import java.util.concurrent.Callable;

import javax.swing.Icon;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.util.ExceptionUtil;

public class InvokeMethodComponentFactory<T extends Component> extends
AbstractComponentFactory<T> {

  private Callable<T> callable;

  public InvokeMethodComponentFactory(final Object object,
    final String methodName, final Object... parameters) {
    this.callable = new InvokeMethodCallable<T>(object, methodName, parameters);
  }

  @Override
  public InvokeMethodComponentFactory<T> clone() {
    return this;
  }

  @Override
  public void close(final Component component) {
    this.callable = null;
  }

  @Override
  public T createComponent() {
    try {
      return this.callable.call();
    } catch (final Exception e) {
      ExceptionUtil.throwUncheckedException(e);
      return null;
    }
  }

  @Override
  public Icon getIcon() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getToolTip() {
    return null;
  }

  @Override
  public String toString() {
    return this.callable.toString();
  }

}

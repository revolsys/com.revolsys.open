package com.revolsys.swing.action.enablecheck;

import java.util.concurrent.Callable;

import com.revolsys.util.ExceptionUtil;

public class CallableEnableCheck<V> extends AbstractValueEnableCheck {
  private Callable<V> callable;

  public CallableEnableCheck(final Object expectedValue, final Callable<V> callable) {
    this(false, expectedValue, callable);
  }

  public CallableEnableCheck(final boolean inverse, final Object expectedValue,
    final Callable<V> callable) {
    super(inverse, expectedValue);
    this.callable = callable;
  }

  @Override
  public Object getValue() {
    try {
      return this.callable.call();
    } catch (final Exception e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public String toString() {
    return this.callable + " " + super.toString();
  }
}

package com.revolsys.swing.action.enablecheck;

import java.util.concurrent.Callable;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.data.equals.Equals;
import com.revolsys.util.ExceptionUtil;

public class InvokeMethodEnableCheck extends AbstractEnableCheck {
  private final Callable<Object> callable;

  private boolean inverse = false;

  private final Object value;

  public InvokeMethodEnableCheck(final boolean inverse, final Object object,
    final String methodName, final Object value) {
    this.callable = new InvokeMethodCallable<Object>(object, methodName);
    this.value = value;
    this.inverse = inverse;
  }

  public InvokeMethodEnableCheck(final Object object, final String methodName) {
    this(object, methodName, true);
  }

  public InvokeMethodEnableCheck(final Object object, final String methodName, final Object value) {
    this(false, object, methodName, value);
  }

  @Override
  public boolean isEnabled() {
    try {
      final Object value = this.callable.call();
      if (Equals.equal(value, this.value) == !this.inverse) {
        return enabled();
      } else {
        return disabled();
      }
    } catch (final Exception e) {
      ExceptionUtil.throwUncheckedException(e);
      return false;
    }
  }

  @Override
  public String toString() {
    return this.callable + "=" + this.value;
  }
}

package com.revolsys.swing.action.enablecheck;

import java.util.concurrent.Callable;

import com.revolsys.beans.InvokeMethodCallable;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.ExceptionUtil;

public class InvokeMethodEnableCheck extends AbstractEnableCheck {
  private final Callable<Object> callable;

  private final Object value;

  private boolean inverse = false;

  public InvokeMethodEnableCheck(final boolean inverse, final Object object,
    final String mthodName, final Object value) {
    this.callable = new InvokeMethodCallable<Object>(object, mthodName, inverse);
    this.value = value;
    this.inverse = inverse;
  }

  public InvokeMethodEnableCheck(final Object object, final String mthodName) {
    this(object, mthodName, true);
  }

  public InvokeMethodEnableCheck(final Object object, final String mthodName,
    final Object value) {
    this(false, object, mthodName, value);
  }

  @Override
  public boolean isEnabled() {
    try {
      final Object value = callable.call();
      if (EqualsRegistry.equal(value, this.value) == !inverse) {
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
    return callable + "=" + value;
  }
}

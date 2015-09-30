package com.revolsys.swing.action.enablecheck;

import com.revolsys.equals.Equals;
import com.revolsys.util.Strings;

public class AbstractValueEnableCheck extends AbstractEnableCheck {
  private boolean inverse = false;

  private final Object expectedValue;

  public AbstractValueEnableCheck(final boolean inverse, final Object expectedValue) {
    this.inverse = inverse;
    this.expectedValue = expectedValue;
  }

  public AbstractValueEnableCheck(final Object expectedValue) {
    this.expectedValue = expectedValue;
  }

  public Object getValue() {
    return null;
  }

  @Override
  public boolean isEnabled() {
    final Object value = getValue();
    final boolean equal = Equals.equal(value, this.expectedValue);
    if (equal == !this.inverse) {
      return enabled();
    } else {
      return disabled();
    }
  }

  @Override
  public String toString() {
    return Strings.toString(this.expectedValue);
  }
}

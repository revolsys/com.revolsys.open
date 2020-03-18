package com.revolsys.swing.action.enablecheck;

import java.util.Collection;

public class AndEnableCheck extends MultiEnableCheck {

  public AndEnableCheck() {
  }

  public AndEnableCheck(final Collection<? extends EnableCheck> enableChecks) {
    super(enableChecks);
  }

  public AndEnableCheck(final EnableCheck... enableChecks) {
    super(enableChecks);
  }

  @Override
  public EnableCheck and(final EnableCheck enableCheck) {
    if (enableCheck == null || enableCheck == this) {
      return this;
    } else if (enableCheck instanceof AndEnableCheck) {
      final AndEnableCheck and = (AndEnableCheck)enableCheck;
      for (final EnableCheck enableCheck2 : and) {
        addEnableCheck(enableCheck2);
      }
      return this;
    } else {
      return new AndEnableCheck(this, enableCheck);
    }
  }

  @Override
  public boolean isEnabled() {
    for (final EnableCheck enableCheck : this) {
      if (!enableCheck.isEnabled()) {
        return disabled();
      }
    }
    return enabled();
  }

  @Override
  public String toString() {
    return "AND " + super.toString();
  }
}

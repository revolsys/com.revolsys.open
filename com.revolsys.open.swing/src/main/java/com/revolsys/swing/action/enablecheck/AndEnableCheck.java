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

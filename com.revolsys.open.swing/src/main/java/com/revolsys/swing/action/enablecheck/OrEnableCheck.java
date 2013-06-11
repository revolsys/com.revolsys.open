package com.revolsys.swing.action.enablecheck;

public class OrEnableCheck extends MultiEnableCheck {
  public OrEnableCheck(final EnableCheck... enableChecks) {
    super(enableChecks);
  }

  @Override
  public boolean isEnabled() {
    for (final EnableCheck enableCheck : this) {
      if (enableCheck.isEnabled()) {
        return enabled();
      }
    }
    return disabled();
  }

  @Override
  public String toString() {
    return "OR " + super.toString();
  }
}

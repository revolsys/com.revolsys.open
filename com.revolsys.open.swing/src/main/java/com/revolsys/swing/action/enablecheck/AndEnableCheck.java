package com.revolsys.swing.action.enablecheck;

import java.util.Collection;
import java.util.Map;

public class AndEnableCheck extends MultiEnableCheck {

  public static AndEnableCheck create(final Map<String, Object> config) {
    return new AndEnableCheck(config);
  }

  public AndEnableCheck() {
  }

  public AndEnableCheck(final Collection<? extends EnableCheck> enableChecks) {
    super(enableChecks);
  }

  public AndEnableCheck(final EnableCheck... enableChecks) {
    super(enableChecks);
  }

  public AndEnableCheck(final Map<String, Object> config) {
    super(config);
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

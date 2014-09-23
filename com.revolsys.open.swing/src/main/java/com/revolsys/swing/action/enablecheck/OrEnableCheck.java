package com.revolsys.swing.action.enablecheck;

import java.util.Map;

public class OrEnableCheck extends MultiEnableCheck {

  public static OrEnableCheck create(final Map<String, Object> config) {
    return new OrEnableCheck(config);
  }

  public OrEnableCheck(final EnableCheck... enableChecks) {
    super(enableChecks);
  }

  public OrEnableCheck(final Map<String, Object> config) {
    super(config);
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

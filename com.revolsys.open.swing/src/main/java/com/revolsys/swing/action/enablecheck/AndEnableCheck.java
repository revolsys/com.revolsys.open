package com.revolsys.swing.action.enablecheck;

public class AndEnableCheck extends MultiEnableCheck {

  @Override
  public boolean isEnabled() {
    for (EnableCheck enableCheck : this) {
      if (!enableCheck.isEnabled()) {
        return disabled();
      }
    }
    return enabled();
  }
  
  @Override
  public String toString() {
    return "AND "  + super.toString();
  }
}

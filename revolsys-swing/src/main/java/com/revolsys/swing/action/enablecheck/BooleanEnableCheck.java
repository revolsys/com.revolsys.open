package com.revolsys.swing.action.enablecheck;

import com.revolsys.beans.PropertyChangeSupportProxy;

public class BooleanEnableCheck extends AbstractEnableCheck implements PropertyChangeSupportProxy {

  public BooleanEnableCheck() {
    this(true);
  }

  public BooleanEnableCheck(final boolean enabled) {
    super(enabled);
  }

  @Override
  public boolean setEnabled(final boolean enabled) {
    return super.setEnabled(enabled);
  }
}

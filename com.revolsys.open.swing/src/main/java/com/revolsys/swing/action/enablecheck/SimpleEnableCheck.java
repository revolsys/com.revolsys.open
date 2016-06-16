package com.revolsys.swing.action.enablecheck;

import com.revolsys.beans.PropertyChangeSupportProxy;

public class SimpleEnableCheck extends AbstractEnableCheck implements PropertyChangeSupportProxy {

  public SimpleEnableCheck() {
    this(true);
  }

  public SimpleEnableCheck(final boolean enabled) {
    super(enabled);
  }

  @Override
  public boolean setEnabled(final boolean enabled) {
    return super.setEnabled(enabled);
  }
}

package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.beans.AbstractPropertyChangeSupportProxy;

public abstract class AbstractEnableCheck extends AbstractPropertyChangeSupportProxy
  implements EnableCheck, PropertyChangeListener {
  private boolean enabled = false;

  public AbstractEnableCheck() {
  }

  public AbstractEnableCheck(final boolean enabled) {
    this.enabled = enabled;
  }

  public boolean disabled() {
    return setEnabled(false);
  }

  public boolean enabled() {
    return setEnabled(true);
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    isEnabled();
  }

  protected boolean setEnabled(final boolean enabled) {
    final boolean oldValue = this.enabled;
    this.enabled = enabled;
    firePropertyChange("enabled", oldValue, enabled);
    return enabled;
  }

  @Override
  public String toString() {
    return Boolean.toString(isEnabled());
  }
}

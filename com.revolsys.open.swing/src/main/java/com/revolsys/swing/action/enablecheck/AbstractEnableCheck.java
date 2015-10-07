package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.beans.AbstractPropertyChangeObject;

public abstract class AbstractEnableCheck extends AbstractPropertyChangeObject
  implements EnableCheck, PropertyChangeListener {
  private boolean enabled = false;

  public boolean disabled() {
    return setEnabled(false);
  }

  public boolean enabled() {
    return setEnabled(true);
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
}

package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.revolsys.beans.AbstractPropertyChangeObject;

public abstract class AbstractEnableCheck extends AbstractPropertyChangeObject
  implements EnableCheck, PropertyChangeListener {

  public boolean disabled() {
    firePropertyChange("enabled", true, false);
    return false;
  }

  public boolean enabled() {
    firePropertyChange("enabled", false, true);
    return true;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    isEnabled();
  }
}

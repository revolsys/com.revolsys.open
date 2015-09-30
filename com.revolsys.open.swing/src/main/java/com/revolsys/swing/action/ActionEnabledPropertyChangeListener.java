package com.revolsys.swing.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import com.revolsys.swing.parallel.Invoke;

public class ActionEnabledPropertyChangeListener implements PropertyChangeListener {

  private final Action action;

  public ActionEnabledPropertyChangeListener(final Action action) {
    this.action = action;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    boolean enabled;
    if (Boolean.TRUE == event.getNewValue()) {
      enabled = true;
    } else {
      enabled = false;
    }
    Invoke.later(() -> this.action.setEnabled(enabled));
  }
}

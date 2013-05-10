package com.revolsys.swing.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

public class ActionEnabledPropertyChangeListener implements
  PropertyChangeListener {

  private final Action action;

  public ActionEnabledPropertyChangeListener(final Action action) {
    this.action = action;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (Boolean.TRUE == event.getNewValue()) {
      action.setEnabled(true);
    } else {
      action.setEnabled(false);
    }
  }
}

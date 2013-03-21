package com.revolsys.swing.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

public class ActionEnabledPropertyChangeListener implements PropertyChangeListener{

  private Action action;
  
  
  public ActionEnabledPropertyChangeListener(Action action) {
     this.action = action;
  }


  public void propertyChange(PropertyChangeEvent event) {
    if (Boolean.TRUE == event.getNewValue()) {
      action.setEnabled(true);
    } else {
      action.setEnabled(false);
    }
  }
}

package com.revolsys.swing.menu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.util.Property;

public class CheckBoxMenuItem extends JCheckBoxMenuItem implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private EnableCheck selectedCheck;

  public CheckBoxMenuItem(final Action action) {
    super(action);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    final boolean enabled = this.selectedCheck.isEnabled();
    setSelected(enabled);
  }

  public void setSelectedCheck(final EnableCheck selectedCheck) {
    Property.removeListener(this.selectedCheck, "enabled", this);
    this.selectedCheck = selectedCheck;
    Property.addListener(selectedCheck, "enabled", this);
    setSelected(selectedCheck == null || this.selectedCheck.isEnabled());
  }
}

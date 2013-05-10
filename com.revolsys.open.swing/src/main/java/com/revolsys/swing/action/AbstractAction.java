package com.revolsys.swing.action;

import javax.swing.Action;
import javax.swing.Icon;

import com.revolsys.swing.action.enablecheck.EnableCheck;

public abstract class AbstractAction extends javax.swing.AbstractAction {

  private final ActionEnabledPropertyChangeListener enabledListener = new ActionEnabledPropertyChangeListener(
    this);

  private boolean checkBox;

  private EnableCheck enableCheck;

  public EnableCheck getEnableCheck() {
    return enableCheck;
  }

  public Icon getIcon() {
    return (Icon)getValue(Action.SMALL_ICON);
  }

  public Integer getMnemonic() {
    return (Integer)getValue(Action.MNEMONIC_KEY);
  }

  public String getName() {
    return (String)getValue(Action.NAME);
  }

  public String getToolTip() {
    return (String)getValue(Action.SHORT_DESCRIPTION);
  }

  public boolean isCheckBox() {
    return checkBox;
  }

  @Override
  public boolean isEnabled() {
    if (enableCheck != null) {
      enableCheck.isEnabled();
    }
    return super.isEnabled();
  }

  protected void setCheckBox(final boolean checkBox) {
    this.checkBox = checkBox;
  }

  public void setEnableCheck(final EnableCheck enableCheck) {
    if (this.enableCheck != null) {
      this.enableCheck.removePropertyChangeListener("enabled", enabledListener);
    }
    this.enableCheck = enableCheck;
    if (this.enableCheck != null) {
      this.enableCheck.addPropertyChangeListener("enabled", enabledListener);
      enableCheck.isEnabled();
    }
  }

  protected void setIcon(final Icon icon) {
    putValue(Action.SMALL_ICON, icon);
  }

  protected void setName(final String name) {
    putValue(Action.NAME, name);
  }

  protected void setToolTip(final String toolTip) {
    putValue(Action.SHORT_DESCRIPTION, toolTip);
  }
}

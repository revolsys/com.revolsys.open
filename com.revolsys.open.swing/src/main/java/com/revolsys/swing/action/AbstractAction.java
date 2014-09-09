package com.revolsys.swing.action;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;

public abstract class AbstractAction extends javax.swing.AbstractAction {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final ActionEnabledPropertyChangeListener enabledListener = new ActionEnabledPropertyChangeListener(
    this);

  private boolean checkBox;

  private EnableCheck enableCheck;

  public JButton createButton() {
    final JButton button = new JButton(this);
    initButton(button);
    return button;
  }

  public JCheckBoxMenuItem createCheckboxMenuItem() {
    final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(this);
    final Icon disabledIcon = getDisabledIcon();
    menuItem.setDisabledIcon(disabledIcon);
    return menuItem;
  }

  public JMenuItem createMenuItem() {
    final JMenuItem menuItem = new JMenuItem(this);
    final Icon disabledIcon = getDisabledIcon();
    menuItem.setDisabledIcon(disabledIcon);
    return menuItem;
  }

  public JToggleButton createToggleButton() {
    final JToggleButton button = new JToggleButton(this);
    initButton(button);
    return button;
  }

  public Icon getDisabledIcon() {
    final Icon icon = getIcon();
    return Icons.getDisabledIcon(icon);
  }

  public EnableCheck getEnableCheck() {
    return this.enableCheck;
  }

  public Icon getIcon() {
    return (Icon)getValue(Action.SMALL_ICON);
  }

  public Object getLargeIcon() {
    return getValue(LARGE_ICON_KEY);
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

  protected void initButton(final AbstractButton button) {
    final Icon icon = getIcon();
    if (icon != null || getLargeIcon() != null) {
      button.setHideActionText(true);
    }
    final Icon disabledIcon = getDisabledIcon();
    button.setDisabledIcon(disabledIcon);
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    button.setFocusPainted(false);
    button.setBorderPainted(false);
  }

  public boolean isCheckBox() {
    return this.checkBox;
  }

  @Override
  public boolean isEnabled() {
    if (this.enableCheck != null) {
      this.enableCheck.isEnabled();
    }
    return super.isEnabled();
  }

  protected void setCheckBox(final boolean checkBox) {
    this.checkBox = checkBox;
  }

  public void setEnableCheck(final EnableCheck enableCheck) {
    if (this.enableCheck != null) {
      this.enableCheck.removeListener("enabled", this.enabledListener);
    }
    this.enableCheck = enableCheck;
    if (this.enableCheck != null) {
      this.enableCheck.addListener("enabled", this.enabledListener);
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

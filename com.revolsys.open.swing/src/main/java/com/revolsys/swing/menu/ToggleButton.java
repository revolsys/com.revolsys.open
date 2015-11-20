package com.revolsys.swing.menu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class ToggleButton extends JToggleButton implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private EnableCheck enableCheck;

  public ToggleButton(final AbstractAction action) {
    super(action);
    final Icon icon = action.getIcon();
    if (icon != null || action.getLargeIcon() != null) {
      setHideActionText(true);
    }
    final Icon disabledIcon = action.getDisabledIcon();
    setDisabledIcon(disabledIcon);
    setHorizontalTextPosition(SwingConstants.CENTER);
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setFocusPainted(false);
    setBorderPainted(false);
    final EnableCheck enableCheck = action.getEnableCheck();
    setEnableCheck(enableCheck);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    final boolean enabled = this.enableCheck.isEnabled();
    Invoke.later(() -> {
      setEnabled(enabled);
    });
  }

  public void setEnableCheck(final EnableCheck enableCheck) {
    Property.removeListener(this.enableCheck, "enabled", this);
    this.enableCheck = enableCheck;
    Property.addListener(enableCheck, "enabled", this);
    setEnabled(enableCheck == null || this.enableCheck.isEnabled());
  }
}

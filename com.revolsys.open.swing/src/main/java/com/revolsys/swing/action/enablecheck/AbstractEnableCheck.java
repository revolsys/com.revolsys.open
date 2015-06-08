package com.revolsys.swing.action.enablecheck;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import com.revolsys.beans.AbstractPropertyChangeObject;
import com.revolsys.swing.tree.MenuSourcePropertyEnableCheck;

public abstract class AbstractEnableCheck extends AbstractPropertyChangeObject implements
  EnableCheck, PropertyChangeListener {

  public static AbstractEnableCheck enableCheck(final Map<String, Object> config) {
    if (config != null) {
      final String type = (String)config.get("type");
      if ("and".equals(type)) {
        return new AndEnableCheck(config);
      } else if ("or".equals(type)) {
        return new OrEnableCheck(config);
      } else if ("menuSourceEnableCheck".equals(type)) {
        return new MenuSourcePropertyEnableCheck(config);
      }
    }
    return null;
  }

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

package com.revolsys.swing.tree;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.util.Property;

public class TreeItemPropertyEnableCheck extends AbstractEnableCheck {
  private final String propertyName;

  private final Object value;

  private boolean inverse = false;

  public TreeItemPropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public TreeItemPropertyEnableCheck(final String propertyName,
    final Object value) {
    this(propertyName, value, false);
  }

  public TreeItemPropertyEnableCheck(final String propertyName,
    final Object value, final boolean inverse) {
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean isEnabled() {
    final Object object = ObjectTree.getMouseClickItem();
    if (object == null) {
      return disabled();
    } else {
      try {
        final Object value = Property.get(object, this.propertyName);
        if (inverse != EqualsRegistry.equal(value, this.value)) {
          return enabled();
        } else {
          return disabled();
        }
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).debug("Enable check not valid", e);
        return disabled();
      }
    }
  }

  @Override
  public String toString() {
    return this.propertyName + "=" + this.value;
  }
}

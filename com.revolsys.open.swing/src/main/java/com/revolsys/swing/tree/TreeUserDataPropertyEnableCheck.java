package com.revolsys.swing.tree;

import org.slf4j.LoggerFactory;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.node.BaseTreeNode;
import com.revolsys.util.Property;

public class TreeUserDataPropertyEnableCheck extends AbstractEnableCheck {
  private final String propertyName;

  private final Object value;

  private boolean inverse = false;

  public TreeUserDataPropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public TreeUserDataPropertyEnableCheck(final String propertyName,
    final Object value) {
    this(propertyName, value, false);
  }

  public TreeUserDataPropertyEnableCheck(final String propertyName,
    final Object value, final boolean inverse) {
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean isEnabled() {
    Object object = MenuFactory.getMenuSource();
    if (object instanceof BaseTreeNode) {
      final BaseTreeNode node = (BaseTreeNode)object;
      object = node.getUserObject();
    }
    if (object == null) {
      return disabled();
    } else {
      try {
        final Object value = Property.get(object, this.propertyName);
        if (this.inverse != EqualsRegistry.equal(value, this.value)) {
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

package com.revolsys.swing.tree;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.util.JavaBeanUtil;

public class TreeItemPropertyEnableCheck extends AbstractEnableCheck {
  private final String propertyName;

  private final Object value;

  public TreeItemPropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public TreeItemPropertyEnableCheck(final String propertyName,
    final Object value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  @Override
  public boolean isEnabled() {
    final Object object = ObjectTree.getMouseClickItem();
    if (object == null) {
      return disabled();
    } else {
      try {
        final Object value = JavaBeanUtil.getValue(object, this.propertyName);
        if (EqualsRegistry.equal(value, this.value)) {
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

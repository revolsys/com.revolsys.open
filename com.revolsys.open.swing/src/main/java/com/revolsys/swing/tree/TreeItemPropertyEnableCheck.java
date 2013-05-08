package com.revolsys.swing.tree;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.util.JavaBeanUtil;

public class TreeItemPropertyEnableCheck extends AbstractEnableCheck {
  private String propertyName;

  private Object value;

  public TreeItemPropertyEnableCheck(String propertyName) {
    this(propertyName, true);
  }

  public TreeItemPropertyEnableCheck(String propertyName, Object value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  @Override
  public boolean isEnabled() {
    Object object = ObjectTree.getMouseClickItem();
    if (object == null) {
      return disabled();
    } else {
      try {
        Object value = JavaBeanUtil.getValue(object, propertyName);
        if (EqualsRegistry.equal(value, this.value)) {
          return enabled();
        } else {
          return disabled();
        }
      } catch (Throwable e) {
        LoggerFactory.getLogger(getClass()).debug("Enable check not valid", e);
        return disabled();
      }
    }
  }

  @Override
  public String toString() {
    return propertyName + "=" + value;
  }
}

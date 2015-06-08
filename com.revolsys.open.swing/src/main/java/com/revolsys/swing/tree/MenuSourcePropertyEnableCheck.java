package com.revolsys.swing.tree;

import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Property;

public class MenuSourcePropertyEnableCheck extends AbstractEnableCheck {

  public static MenuSourcePropertyEnableCheck create(final Map<String, Object> config) {
    return new MenuSourcePropertyEnableCheck(config);
  }

  private final String propertyName;

  private final Object value;

  private boolean inverse = false;

  public MenuSourcePropertyEnableCheck(final Map<String, Object> config) {
    this.propertyName = (String)config.get("propertyName");
    this.value = config.get("value");
    this.inverse = Maps.getBool(config, "inverse");
  }

  public MenuSourcePropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public MenuSourcePropertyEnableCheck(final String propertyName, final Object value) {
    this(propertyName, value, false);
  }

  public MenuSourcePropertyEnableCheck(final String propertyName, final Object value,
    final boolean inverse) {
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean isEnabled() {
    final Object object = MenuFactory.getMenuSource();
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

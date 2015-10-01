package com.revolsys.swing.menu;

import java.util.function.Function;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;

public class MenuSourceCallableEnableCheck<T> extends AbstractEnableCheck {
  private boolean inverse = false;

  private Function<T, Boolean> function;

  public MenuSourceCallableEnableCheck(final Function<T, Boolean> function) {
    this(function, false);
  }

  public MenuSourceCallableEnableCheck(final Function<T, Boolean> function, final boolean inverse) {
    this.function = function;
    this.inverse = inverse;
  }

  @Override
  public boolean isEnabled() {
    final T item = MenuFactory.getMenuSource();
    if (item == null) {
      return disabled();
    } else {
      try {
        final boolean enabled = this.function.apply(item);
        if (this.inverse != enabled) {
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
    return this.function.toString();
  }
}

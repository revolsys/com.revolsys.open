package com.revolsys.util.enableable;

public class ThreadEnableable implements Enableable {

  private final ThreadLocal<Boolean> enabled = new ThreadLocal<>();

  @Override
  public boolean isEnabled() {
    if (this.enabled.get() != Boolean.FALSE) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean setEnabled(final boolean enabled) {
    final boolean oldValue = this.enabled.get() != Boolean.FALSE;
    if (enabled) {
      this.enabled.set(null);
    } else {
      this.enabled.set(Boolean.FALSE);
    }
    return oldValue;
  }

  @Override
  public String toString() {
    return Boolean.toString(isEnabled());
  }
}

package com.revolsys.util.enableable;

public class GlobalEnableable implements Enableable {

  private boolean enabled = true;

  @Override
  public synchronized boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public synchronized boolean setEnabled(final boolean enabled) {
    final boolean oldValue = this.enabled;
    this.enabled = enabled;
    return oldValue;
  }

  @Override
  public String toString() {
    return Boolean.toString(this.enabled);
  }
}

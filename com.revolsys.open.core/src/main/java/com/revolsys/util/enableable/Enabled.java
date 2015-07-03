package com.revolsys.util.enableable;

public class Enabled implements AutoCloseable {
  private boolean originalEnabled;

  private final Enableable enableable;

  private final boolean enabled;

  public Enabled(final Enableable enableable, final boolean enabled) {
    this.enableable = enableable;
    this.enabled = enabled;
    if (enableable != null) {
      this.originalEnabled = enableable.setEnabled(enabled);
    }
  }

  @Override
  public void close() {
    if (this.enableable != null) {
      this.enableable.setEnabled(this.originalEnabled);
    }
  }

  public boolean isEventsEnabled() {
    return this.enabled;
  }

  @Override
  public String toString() {
    return Boolean.toString(this.enabled);
  }
}

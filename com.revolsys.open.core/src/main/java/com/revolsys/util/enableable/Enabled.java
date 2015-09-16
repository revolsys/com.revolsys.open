package com.revolsys.util.enableable;

import java.io.Closeable;

public class Enabled implements Closeable {
  private final Enableable enableable;

  private final boolean enabled;

  private boolean originalEnabled;

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

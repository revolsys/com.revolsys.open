package com.revolsys.util.enableable;

public interface Enableable {
  default Enabled disabled() {
    return new Enabled(this, false);
  }

  default Enabled enabled() {
    return new Enabled(this, true);
  }

  boolean isEnabled();

  boolean setEnabled(boolean enabled);
}

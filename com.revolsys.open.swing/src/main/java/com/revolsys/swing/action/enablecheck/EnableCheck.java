package com.revolsys.swing.action.enablecheck;

@FunctionalInterface
public interface EnableCheck {
  EnableCheck ENABLED = () -> {
    return true;
  };

  EnableCheck DISABLED = () -> {
    return false;
  };

  boolean isEnabled();
}

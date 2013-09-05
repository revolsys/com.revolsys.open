package com.revolsys.swing.preferences;

import java.awt.Component;

public interface PreferencesPanel {
  void cancelChanges();

  Component getComponent();

  String getTitle();

  boolean isPreferencesValid();

  boolean savePreferences();
}

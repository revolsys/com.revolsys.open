package com.revolsys.swing.preferences;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import com.revolsys.swing.field.Field;
import com.revolsys.util.PreferenceKey;

public class SimplePreferencesPanel extends AbstractPreferencesPanel {
  private static final long serialVersionUID = 1L;

  private final Set<Preference> preferences = new LinkedHashSet<>();

  public SimplePreferencesPanel(final String title) {
    super(title, null);
  }

  public Preference addPreference(final String applicationName, final PreferenceKey preferenceKey,
    final Function<Preference, Field> fieldFactory) {
    final Preference preference = new Preference(applicationName, preferenceKey, fieldFactory);
    if (this.preferences.contains(preference)) {
      for (final Preference preference2 : this.preferences) {
        if (preference2.equals(preference)) {
          return preference2;
        }
      }
      return preference;
    } else {
      this.preferences.add(preference);
      addField(preference.getField());
      return preference;
    }
  }

  @Override
  public void cancelChanges() {
    for (final Preference preference : this.preferences) {
      preference.cancelChanges();
    }
  }

  @Override
  protected void doSavePreferences() {
    for (final Preference preference : this.preferences) {
      preference.saveChanges();
    }
  }
}

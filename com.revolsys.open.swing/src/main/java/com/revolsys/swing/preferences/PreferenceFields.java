package com.revolsys.swing.preferences;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.swing.field.Field;

public class PreferenceFields {
  private static final Map<String, List<Consumer<PreferencesDialog>>> fieldFactories = new LinkedHashMap<>();

  public static void addField(final String title, final String applicationName, final String path,
    final String propertyName, final DataType valueClass, final Object defaultValue) {
    addField(title, applicationName, path, propertyName, valueClass, defaultValue, null);
  }

  public static void addField(final String title, final String applicationName, final String path,
    final String propertyName, final DataType valueClass, final Object defaultValue,
    final Function<Preference, Field> fieldFactory) {

    final Consumer<PreferencesDialog> factory = (Consumer<PreferencesDialog>)(dialog) -> {
      PreferencesPanel panel = dialog.getPanel(title);
      if (panel == null) {
        panel = new SimplePreferencesPanel(title);
        dialog.addPanel(panel);
      }
      if (panel instanceof SimplePreferencesPanel) {
        final SimplePreferencesPanel simplePanel = (SimplePreferencesPanel)panel;
        simplePanel.addPreference(applicationName, path, propertyName, valueClass, defaultValue,
          fieldFactory);
      }
    };
    Maps.addToList(fieldFactories, title, factory);
  }

  public static void initDialog(final PreferencesDialog dialog) {
    for (final List<Consumer<PreferencesDialog>> factories : fieldFactories.values()) {
      for (final Consumer<PreferencesDialog> factory : factories) {
        factory.accept(dialog);
      }
    }
  }
}

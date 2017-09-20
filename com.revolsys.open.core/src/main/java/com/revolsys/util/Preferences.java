package com.revolsys.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.json.Json;

public class Preferences {

  private String applicationId;

  public Preferences(final String applicationId) {
    this.applicationId = applicationId;
  }

  public boolean getBoolean(final PreferenceKey preference) {
    final Object value = getValue(preference);
    return !Booleans.isFalse(value);
  }

  public boolean getBoolean(final PreferenceKey preference, final boolean defaultValue) {
    final Object value = getValue(preference);
    if (value == null) {
      return defaultValue;
    } else {
      return !Booleans.isFalse(value);
    }
  }

  public Integer getInt(final PreferenceKey preference) {
    final Object value = getValue(preference);
    if (value == null) {
      return null;
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  public int getInt(final PreferenceKey preference, final int defaultValue) {
    final Object value = getValue(preference);
    if (value == null) {
      return defaultValue;
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  public File getPreferenceFile(final PreferenceKey preference) {
    final String path = preference.getPath();
    if (path.contains("..")) {
      throw new IllegalArgumentException(
        "Path cannot contain the '..' character sequernce: " + path);
    }
    final File preferencesDirectory = getPreferencesDirectory();
    final File file = FileUtil.getFile(preferencesDirectory, path + ".rgobject");
    file.getParentFile().mkdirs();
    return file;
  }

  public Map<String, Object> getPreferences(final PreferenceKey preference) {
    final File file = getPreferenceFile(preference);
    if (file.exists()) {
      return Json.toMap(file);
    } else {
      return new LinkedHashMap<>();
    }
  }

  public File getPreferencesDirectory() {
    String path;
    if (OS.isWindows()) {
      path = System.getenv("APPDATA") + "/" + this.applicationId + "/Preferences";
    } else if (OS.isMac()) {
      path = System.getProperty("user.home") + "/Library/Preferences/" + this.applicationId;
    } else {
      path = System.getProperty("user.home") + "/.config/" + this.applicationId + "/Preferences";
    }
    final File directory = FileUtil.getFile(path);
    directory.mkdirs();
    return directory;
  }

  public <T> T getValue(final PreferenceKey preference) {
    return getValue(preference, null);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(final PreferenceKey preference, final T defaultValue) {
    final Map<String, Object> preferences = getPreferences(preference);
    if (preferences == null) {
      return defaultValue;
    } else {
      final String name = preference.getName();
      return (T)preferences.getOrDefault(name, defaultValue);
    }
  }

  public void setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
  }

  public void setValue(final PreferenceKey preference, final Object value) {
    final Map<String, Object> preferences = getPreferences(preference);
    final String name = preference.getName();
    preferences.put(name, value);
    final File file = getPreferenceFile(preference);
    Json.writeMap(preferences, file, true);
  }

}

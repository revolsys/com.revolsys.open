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

  public boolean getBoolean(final String path, final String propertyName) {
    final Map<String, Object> preferences = getPreferences(path);
    final Object value = preferences.get(propertyName);
    return !Booleans.isFalse(value);
  }

  public boolean getBoolean(final String path, final String propertyName,
    final boolean defaultValue) {
    final Map<String, Object> preferences = getPreferences(path);
    final Object value = preferences.get(propertyName);
    if (value == null) {
      return defaultValue;
    } else {
      return !Booleans.isFalse(value);
    }
  }

  public Integer getInt(final String path, final String propertyName) {
    final Map<String, Object> preferences = getPreferences(path);
    final Object value = preferences.get(propertyName);
    if (value == null) {
      return null;
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  public int getInt(final String path, final String propertyName, final int defaultValue) {
    final Map<String, Object> preferences = getPreferences(path);
    final Object value = preferences.get(propertyName);
    if (value == null) {
      return defaultValue;
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  public File getPreferenceFile(final String path) {
    if (path.contains("..")) {
      throw new IllegalArgumentException(
        "Path cannot contain the '..' character sequernce: " + path);
    }
    final File preferencesDirectory = getsDirectory();
    final File file = FileUtil.getFile(preferencesDirectory, path + ".rgobject");
    file.getParentFile().mkdirs();
    return file;
  }

  public Map<String, Object> getPreferences(final String path) {
    final File file = getPreferenceFile(path);
    if (file.exists()) {
      return Json.toMap(file);
    } else {
      return new LinkedHashMap<>();
    }
  }

  public File getsDirectory() {
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

  @SuppressWarnings("unchecked")
  public <T> T getValue(final String path, final String propertyName) {
    final Map<String, Object> preferences = getPreferences(path);
    return (T)Property.get(preferences, propertyName);
  }

  public <T> T getValue(final String path, final String propertyName, final T defaultValue) {
    final T value = getValue(path, propertyName);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public void setApplicationId(final String applicationId) {
    this.applicationId = applicationId;
  }

  public void setValue(final String path, final String propertyName, final Object value) {
    final Map<String, Object> preferences = getPreferences(path);
    preferences.put(propertyName, value);
    final File file = getPreferenceFile(path);
    Json.writeMap(preferences, file, true);
  }

}

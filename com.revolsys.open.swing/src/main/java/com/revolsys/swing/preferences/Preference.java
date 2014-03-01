package com.revolsys.swing.preferences;

import javax.swing.JComponent;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.util.OS;

public class Preference {

  private final String applicationName;

  private final String path;

  private final String propertyName;

  private final Class<?> valueClass;

  private Object savedValue;

  private Object value;

  private final Field field;

  private final JComponent fieldComponent;

  public Preference(final String applicationName, final String path,
    final String propertyName, final Class<?> valueClass,
    final Object defaultValue) {
    this(applicationName, path, propertyName, valueClass, defaultValue,
      (JComponent)null);
  }

  public Preference(final String applicationName, final String path,
    final String propertyName, final Class<?> valueClass,
    final Object defaultValue, final Field field) {
    this(applicationName, path, propertyName, valueClass, defaultValue,
      (JComponent)field);
  }

  public Preference(final String applicationName, final String path,
    final String propertyName, final Class<?> valueClass,
    final Object defaultValue, final JComponent field) {
    this.applicationName = applicationName;
    this.path = path;
    this.propertyName = propertyName;
    this.valueClass = valueClass;
    this.savedValue = OS.getPreference(applicationName, path, propertyName,
      defaultValue);
    if (field == null) {
      this.fieldComponent = SwingUtil.createField(valueClass, propertyName,
        defaultValue);
    } else {
      this.fieldComponent = field;
    }
    this.field = (Field)this.fieldComponent;
    cancelChanges();
  }

  public void cancelChanges() {
    field.setFieldValue(StringConverterRegistry.toObject(valueClass, savedValue));
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof Preference) {
      final Preference other = (Preference)object;
      if (EqualsRegistry.equal(other.applicationName, applicationName)) {
        if (EqualsRegistry.equal(other.path, path)) {
          if (EqualsRegistry.equal(other.propertyName, propertyName)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public Field getField() {
    return field;
  }

  public JComponent getFieldComponent() {
    return fieldComponent;
  }

  public String getPath() {
    return path;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public Object getSavedValue() {
    return savedValue;
  }

  public Object getValue() {
    return value;
  }

  public Class<?> getValueClass() {
    return valueClass;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (applicationName != null) {
      result = prime * result + applicationName.hashCode();
    }
    if (path != null) {
      result = prime * result + path.hashCode();
    }
    if (propertyName != null) {
      result = prime * result + propertyName.hashCode();
    }
    return result;
  }

  public boolean isValid() {
    return field.isFieldValid();
  }

  public void saveChanges() {
    final Object value = field.getFieldValue();
    OS.setPreference(applicationName, path, propertyName, value);
    savedValue = value;
  }
}

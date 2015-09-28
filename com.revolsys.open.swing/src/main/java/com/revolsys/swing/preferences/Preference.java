package com.revolsys.swing.preferences;

import javax.swing.JComponent;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.equals.Equals;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.util.OS;

public class Preference {

  private final String applicationName;

  private final Field field;

  private final JComponent fieldComponent;

  private final String path;

  private final String propertyName;

  private Object savedValue;

  private Object value;

  private final Class<?> valueClass;

  public Preference(final String applicationName, final String path, final String propertyName,
    final Class<?> valueClass, final Object defaultValue) {
    this(applicationName, path, propertyName, valueClass, defaultValue, (JComponent)null);
  }

  public Preference(final String applicationName, final String path, final String propertyName,
    final Class<?> valueClass, final Object defaultValue, final Field field) {
    this(applicationName, path, propertyName, valueClass, defaultValue, (JComponent)field);
  }

  public Preference(final String applicationName, final String path, final String propertyName,
    final Class<?> valueClass, final Object defaultValue, final JComponent field) {
    this.applicationName = applicationName;
    this.path = path;
    this.propertyName = propertyName;
    this.valueClass = valueClass;
    this.savedValue = OS.getPreference(applicationName, path, propertyName, defaultValue);
    if (field == null) {
      this.fieldComponent = SwingUtil.newField(valueClass, propertyName, defaultValue);
    } else {
      this.fieldComponent = field;
    }
    this.field = (Field)this.fieldComponent;
    cancelChanges();
  }

  public void cancelChanges() {
    this.field.setFieldValue(StringConverterRegistry.toObject(this.valueClass, this.savedValue));
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof Preference) {
      final Preference other = (Preference)object;
      if (Equals.equal(other.applicationName, this.applicationName)) {
        if (Equals.equal(other.path, this.path)) {
          if (Equals.equal(other.propertyName, this.propertyName)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public String getApplicationName() {
    return this.applicationName;
  }

  public Field getField() {
    return this.field;
  }

  public JComponent getFieldComponent() {
    return this.fieldComponent;
  }

  public String getPath() {
    return this.path;
  }

  public String getPropertyName() {
    return this.propertyName;
  }

  public Object getSavedValue() {
    return this.savedValue;
  }

  public Object getValue() {
    return this.value;
  }

  public Class<?> getValueClass() {
    return this.valueClass;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.applicationName != null) {
      result = prime * result + this.applicationName.hashCode();
    }
    if (this.path != null) {
      result = prime * result + this.path.hashCode();
    }
    if (this.propertyName != null) {
      result = prime * result + this.propertyName.hashCode();
    }
    return result;
  }

  public boolean isValid() {
    return this.field.isFieldValid();
  }

  public void saveChanges() {
    final Object value = this.field.getFieldValue();
    OS.setPreference(this.applicationName, this.path, this.propertyName, value);
    this.savedValue = value;
  }
}

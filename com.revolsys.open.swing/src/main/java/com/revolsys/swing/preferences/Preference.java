package com.revolsys.swing.preferences;

import java.util.function.Function;

import javax.swing.JComponent;

import org.jeometry.common.data.type.DataType;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.util.PreferenceKey;
import com.revolsys.util.Preferences;

public class Preference implements PropertyChangeSupportProxy {
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private final String applicationName;

  private final Field field;

  private final JComponent fieldComponent;

  private final PreferenceKey preference;

  private Object savedValue;

  private Object value;

  private final DataType dataType;

  private final Preferences preferences;

  public Preference(final String applicationName, final PreferenceKey preference,
    final Function<Preference, Field> fieldFactory) {
    this.applicationName = applicationName;
    this.preferences = new Preferences(applicationName);
    this.preference = preference;

    this.dataType = preference.getDataType();
    final Object defaultValue = preference.getDefaultValue();
    this.savedValue = this.preferences.getValue(preference);
    final String propertyName = preference.getName();
    if (fieldFactory == null) {
      this.fieldComponent = SwingUtil.newField(this.dataType, propertyName, defaultValue);
    } else {
      this.fieldComponent = (JComponent)fieldFactory.apply(this);
    }
    this.field = (Field)this.fieldComponent;
    cancelChanges();
  }

  public void cancelChanges() {
    final Object convertedValue = this.dataType.toObject(this.savedValue);
    this.field.setFieldValue(convertedValue);
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof Preference) {
      final Preference other = (Preference)object;
      if (DataType.equal(other.applicationName, this.applicationName)) {
        if (DataType.equal(other.preference, this.preference)) {
          return true;
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

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  public Object getSavedValue() {
    return this.savedValue;
  }

  public Object getValue() {
    return this.value;
  }

  public DataType getValueClass() {
    return this.dataType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.applicationName != null) {
      result = prime * result + this.applicationName.hashCode();
    }
    if (this.preference != null) {
      result = prime * result + this.preference.hashCode();
    }
    return result;
  }

  public boolean isValid() {
    return this.field.isFieldValid();
  }

  public void saveChanges() {
    final Object oldValue = this.savedValue;
    final Object value = this.field.getFieldValue();
    this.preferences.setValue(this.preference, value);
    this.savedValue = value;
    final String name = this.preference.getName();
    firePropertyChange(name, oldValue, value);

  }
}

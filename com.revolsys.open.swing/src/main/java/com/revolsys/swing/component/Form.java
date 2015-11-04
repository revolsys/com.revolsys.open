package com.revolsys.swing.component;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;
import com.revolsys.util.function.Consumer2;

public class Form extends JPanel {
  private static final long serialVersionUID = 1L;

  private final Map<String, Field> fieldByName = new HashMap<>();

  private final PropertyChangeListener propertyChangeSetValue = Property
    .newListener(this::setFieldValue);

  private final List<Consumer2<String, Object>> fieldValueListeners = new ArrayList<>();

  private final Map<String, List<Consumer2<String, Object>>> fieldValueListenersByFieldName = new HashMap<>();

  public void addField(final Field field) {
    setField(field);
    add((JComponent)field);
  }

  public boolean addFieldValueListener(final Consumer2<String, Object> listener) {
    if (this.fieldValueListeners.contains(listener)) {
      return false;
    } else {
      return this.fieldValueListeners.add(listener);
    }
  }

  @SuppressWarnings("unchecked")
  public <V> boolean addFieldValueListener(final String fieldName, final Consumer<V> listener) {
    return addFieldValueListener(fieldName, (name, value) -> {
      listener.accept((V)value);
    });
  }

  public boolean addFieldValueListener(final String fieldName,
    final Consumer2<String, Object> listener) {
    final List<Consumer2<String, Object>> listeners = Maps
      .getList(this.fieldValueListenersByFieldName, fieldName);
    if (listeners.contains(listener)) {
      return false;
    } else {
      return listeners.add(listener);
    }
  }

  public void addLabelAndField(final Field field) {
    if (field != null) {
      final String fieldName = field.getFieldName();
      SwingUtil.addLabel(this, fieldName);
      setField(field);
      add((JComponent)field);
    }
  }

  public <F> F addLabelAndNewField(final String fieldName, final DataType dataType) {
    SwingUtil.addLabel(this, fieldName);
    return addNewField(fieldName, dataType);
  }

  @SuppressWarnings("unchecked")
  public <F> F addNewField(final String fieldName, final DataType dataType) {
    final Field field = newField(fieldName, dataType);
    addField(field);
    return (F)field;
  }

  @Override
  public void addNotify() {
    super.addNotify();
    for (final Entry<String, Field> entry : this.fieldByName.entrySet()) {
      final String fieldName = entry.getKey();
      final Field field = entry.getValue();
      Property.addListener(field, fieldName, this.propertyChangeSetValue);
    }
  }

  @SuppressWarnings("unchecked")
  public <F> F getField(final String filedName) {
    return (F)this.fieldByName.get(filedName);
  }

  public <V> V getFieldValue(final String fieldName) {
    final Field field = getField(fieldName);
    if (field == null) {
      return null;
    } else {
      return field.getFieldValue();
    }
  }

  @SuppressWarnings("unchecked")
  public <F> F newField(final String fieldName, final DataType dataType) {
    return (F)SwingUtil.newField(dataType, fieldName, null);
  }

  public boolean removeFieldValueListener(final Consumer2<String, Object> listener) {
    return this.fieldValueListeners.remove(listener);
  }

  @Override
  public void removeNotify() {
    super.addNotify();
    for (final Entry<String, Field> entry : this.fieldByName.entrySet()) {
      final String fieldName = entry.getKey();
      final Field field = entry.getValue();
      Property.removeListener(field, fieldName, this.propertyChangeSetValue);
    }
  }

  public void save() {
  }

  public void setField(final Field field) {
    if (field != null) {
      Invoke.later(() -> {
        final String fieldName = field.getFieldName();
        final Field oldField = this.fieldByName.put(fieldName, field);
        if (oldField != null) {
          Property.removeListener(oldField, fieldName, this.propertyChangeSetValue);
        }
        if (isDisplayable()) {
          Property.addListener(field, fieldName, this.propertyChangeSetValue);
        }
      });
    }
  }

  public boolean setFieldValue(final String fieldName, final Object value) {
    final Field field = getField(fieldName);
    if (field == null) {
      return false;
    } else {
      final boolean modified = field.setFieldValue(value);
      for (final Consumer2<String, Object> listener : this.fieldValueListeners) {
        try {
          listener.accept(fieldName, field.getFieldValue());
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error("Cannot set " + field + "=" + value, e);
        }
      }
      final List<Consumer2<String, Object>> listeners = this.fieldValueListenersByFieldName
        .get(fieldName);
      if (listeners != null) {
        for (final Consumer2<String, Object> listener : listeners) {
          try {
            listener.accept(fieldName, field.getFieldValue());
          } catch (final Throwable e) {
            LoggerFactory.getLogger(getClass()).error("Cannot set " + field + "=" + value, e);
          }
        }
      }
      return modified;
    }

  }

  public void setFieldValues(final Map<String, ? extends Object> values) {
    if (values != null) {
      for (final Entry<String, ? extends Object> entry : values.entrySet()) {
        final String name = entry.getKey();
        final Object value = entry.getValue();
        setFieldValue(name, value);
      }
    }
  }
}

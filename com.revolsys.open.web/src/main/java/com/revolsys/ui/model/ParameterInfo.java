package com.revolsys.ui.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.data.type.DataType;

public class ParameterInfo {
  private final Map<Object, Object> allowedValues = new LinkedHashMap<>();

  private Object defaultValue;

  private final String description;

  private final String name;

  private final boolean required;

  private final DataType type;

  public ParameterInfo(final String name, final boolean required, final DataType type,
    final String description) {
    this(name, required, type, description, Collections.emptyList());
  }

  public ParameterInfo(final String name, final boolean required, final DataType type,
    final String description, final List<?> allowedValues) {
    this.name = name;
    this.required = required;
    this.type = type;
    this.description = description;
    for (final Object allowedValue : allowedValues) {
      this.allowedValues.put(allowedValue, allowedValue);
    }
  }

  public ParameterInfo(final String name, final boolean required, final DataType type,
    final String description, final Map<?, ?> allowedValues) {
    this.name = name;
    this.required = required;
    this.type = type;
    this.description = description;
    for (final Entry<?, ?> allowedValue : allowedValues.entrySet()) {
      final Object key = allowedValue.getKey();
      final Object value = allowedValue.getValue();
      this.allowedValues.put(key, value);
    }
  }

  public ParameterInfo(final String name, final boolean required, final DataType type,
    final String description, final Object defaultValue, final Map<?, ?> allowedValues) {
    this.name = name;
    this.required = required;
    this.type = type;
    this.description = description;
    this.defaultValue = defaultValue;
    for (final Entry<?, ?> allowedValue : allowedValues.entrySet()) {
      final Object key = allowedValue.getKey();
      final Object value = allowedValue.getValue();
      this.allowedValues.put(key, value);
    }
  }

  public void addAllowedValue(final Object value, final Object text) {
    this.allowedValues.put(value, text);
  }

  public Map<Object, Object> getAllowedValues() {
    return this.allowedValues;
  }

  public Object getDefaultValue() {
    return this.defaultValue;
  }

  public String getDescription() {
    return this.description;
  }

  public String getName() {
    return this.name;
  }

  public DataType getType() {
    return this.type;
  }

  public boolean isRequired() {
    return this.required;
  }
}

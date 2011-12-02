package com.revolsys.ui.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.gis.data.model.types.DataType;

public class ParameterInfo {
  private final String name;

  private final boolean required;

  private final DataType type;

  private final String description;

  private final Map<Object, Object> allowedValues = new LinkedHashMap<Object, Object>();

  private Object defaultValue;

  public ParameterInfo(final String name, final boolean required,
    final DataType type, final String description) {
    this(name, required, type, description, Collections.emptyList());
  }

  public ParameterInfo(final String name, final boolean required,
    final DataType type, final String description,
    final Object... allowedValues) {
    this(name, required, type, description, Arrays.asList(allowedValues));
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public ParameterInfo(final String name, final boolean required,
    final DataType type, final String description, final List<?> allowedValues) {
    this.name = name;
    this.required = required;
    this.type = type;
    this.description = description;
    for (Object allowedValue : allowedValues) {
      this.allowedValues.put(allowedValue, allowedValue);
    }
  }

  public ParameterInfo(final String name, final boolean required,
    final DataType type, final String description, final Map<?, ?> allowedValues) {
    this.name = name;
    this.required = required;
    this.type = type;
    this.description = description;
    for (Entry<?, ?> allowedValue : allowedValues.entrySet()) {
      final Object key = allowedValue.getKey();
      final Object value = allowedValue.getValue();
      this.allowedValues.put(key, value);
    }
  }

  public void addAllowedValue(Object value, Object text) {
    this.allowedValues.put(value, text);
  }

  public Map<Object, Object> getAllowedValues() {
    return allowedValues;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public DataType getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

}

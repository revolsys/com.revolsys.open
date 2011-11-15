package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.data.model.types.DataType;

public class ParameterInfo {
  private final String name;

  private final boolean required;

  private final DataType type;

  private final String description;

  private final List<Object> allowedValues = new ArrayList<Object>();

  public ParameterInfo(final String name, final boolean required,
    final DataType type, final String description, final Object... allowedValues) {
    this(name, required, type, description, Arrays.asList(allowedValues));
  }

  public ParameterInfo(final String name, final boolean required,
    final DataType type, final String description,
    final List<?> allowedValues) {
    this.name = name;
    this.required = required;
    this.type = type;
    this.description = description;
    this.allowedValues.addAll(allowedValues);
  }

  public List<Object> getAllowedValues() {
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

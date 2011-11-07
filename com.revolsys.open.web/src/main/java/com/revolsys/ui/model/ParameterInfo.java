package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParameterInfo {
  private final String name;

  private final boolean required;

  private final String type;

  private final String description;

  private final List<Object> allowedValues = new ArrayList<Object>();

  public ParameterInfo(final String name, final boolean required,
    final String type, final String description, final Object... allowedValues) {
    this(name, required, type, description, Arrays.asList(allowedValues));
  }

  public ParameterInfo(final String name, final boolean required,
    final String type, final String description,
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

  public String getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

}

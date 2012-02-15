package com.revolsys.gis.data.model.types;

import java.util.Set;

import javax.xml.namespace.QName;

public class EnumerationDataType extends SimpleDataType {

  private final Set<String> allowedValues;

  public EnumerationDataType(final QName name, final Class<?> javaClass,
    final Set<String> allowedValues) {
    super(name, javaClass);
    this.allowedValues = allowedValues;
  }

  public Set<String> getAllowedValues() {
    return allowedValues;
  }

}

package com.revolsys.io.esri.gdb.xml.model;

public class SubtypeFieldInfo {
  private String fieldName;

  private String domainName;

  private Object defaultValue;

  public Object getDefaultValue() {
    return this.defaultValue;
  }

  public String getDomainName() {
    return this.domainName;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public void setDefaultValue(final Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setDomainName(final String domainName) {
    this.domainName = domainName;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

}

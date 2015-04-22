package com.revolsys.format.esri.gdb.xml.model;

import com.revolsys.format.esri.gdb.xml.model.enums.FieldType;

public class Field {
  private String name;

  private FieldType type;

  private boolean isNullable;

  private int length;

  private int precision;

  private int scale;

  private Boolean required;

  private Boolean editable;

  private String aliasName;

  private String modelName;

  private GeometryDef geometryDef;

  private Boolean domainFixed;

  private Object defaultValue;

  private Domain domain;

  // TODO RasterDef rasterDef

  public String getAliasName() {
    return this.aliasName;
  }

  public Object getDefaultValue() {
    return this.defaultValue;
  }

  public Domain getDomain() {
    return this.domain;
  }

  public Boolean getDomainFixed() {
    return this.domainFixed;
  }

  public Boolean getEditable() {
    return this.editable;
  }

  public GeometryDef getGeometryDef() {
    return this.geometryDef;
  }

  public int getLength() {
    return this.length;
  }

  public String getModelName() {
    return this.modelName;
  }

  public String getName() {
    return this.name;
  }

  public int getPrecision() {
    return this.precision;
  }

  public Boolean getRequired() {
    return this.required;
  }

  public int getScale() {
    return this.scale;
  }

  public FieldType getType() {
    return this.type;
  }

  public boolean isIsNullable() {
    return this.isNullable;
  }

  public void setAliasName(final String aliasName) {
    this.aliasName = aliasName;
  }

  public void setDefaultValue(final Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setDomain(final Domain domain) {
    this.domain = domain;
  }

  public void setDomainFixed(final Boolean domainFixed) {
    this.domainFixed = domainFixed;
  }

  public void setEditable(final Boolean editable) {
    this.editable = editable;
  }

  public void setGeometryDef(final GeometryDef geometryDef) {
    this.geometryDef = geometryDef;
  }

  public void setIsNullable(final boolean isNullable) {
    this.isNullable = isNullable;
  }

  public void setLength(final int length) {
    this.length = length;
  }

  public void setModelName(final String modelName) {
    this.modelName = modelName;
  }

  public void setName(final String name) {
    this.name = name;
    if (this.aliasName == null) {
      this.aliasName = name;
    }
    if (this.modelName == null) {
      this.modelName = name;
    }
  }

  public void setPrecision(final int precision) {
    this.precision = precision;
  }

  public void setRequired(final Boolean required) {
    this.required = required;
  }

  public void setScale(final int scale) {
    this.scale = scale;
  }

  public void setType(final FieldType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return this.name + ":" + this.type;
  }
}

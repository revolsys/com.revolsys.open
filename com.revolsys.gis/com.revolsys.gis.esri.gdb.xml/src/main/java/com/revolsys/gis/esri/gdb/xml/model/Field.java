package com.revolsys.gis.esri.gdb.xml.model;

import com.revolsys.gis.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.gis.util.NoOp;

public class Field {
  private String name;

  private FieldType type;

  private boolean isNullable;

  private int length;

  private int precision;

  private int scale;

  private boolean required;

  private boolean editable = true;

  private String aliasName;

  private String modelName;

  private GeometryDef geometryDef;

  private Boolean domainFixed;

  private Object defaultValue;

  private Domain domain;

  // TODO RasterDef rasterDef

  public String getAliasName() {
    return aliasName;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public Domain getDomain() {
    return domain;
  }

  public Boolean getDomainFixed() {
    return domainFixed;
  }

  public GeometryDef getGeometryDef() {
    return geometryDef;
  }

  public int getLength() {
    return length;
  }

  public String getModelName() {
    return modelName;
  }

  public String getName() {
    return name;
  }

  public int getPrecision() {
    return precision;
  }

  public int getScale() {
    return scale;
  }

  public FieldType getType() {
    return type;
  }

  public boolean isEditable() {
    return editable;
  }

  public boolean isIsNullable() {
    return isNullable;
  }

  public boolean isRequired() {
    return required;
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

  public void setEditable(final boolean editable) {
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
    if (aliasName == null) {
      this.aliasName = name;
    }
    if (modelName == null) {
      this.modelName = name;
    }
  }

  public void setPrecision(final int precision) {
    this.precision = precision;
  }

  public void setRequired(final boolean required) {
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
    return name + ":" + type;
  }
}

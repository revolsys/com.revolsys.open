package com.revolsys.gis.esri.gdb.xml.parser;

public class Field {
  private String name;

  private String type;

  private boolean isNullable;

  private int length;

  private int precision;

  private int scale;

  private boolean required;

  private boolean editable;

  private String aliasName;

  private String modelName;

  private GeometryDef geometryDef;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isIsNullable() {
    return isNullable;
  }

  public void setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
  }

  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public int getScale() {
    return scale;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public String getAliasName() {
    return aliasName;
  }

  public void setAliasName(String aliasName) {
    this.aliasName = aliasName;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public GeometryDef getGeometryDef() {
    return geometryDef;
  }

  public void setGeometryDef(GeometryDef geometryDef) {
    this.geometryDef = geometryDef;
  }

  @Override
  public String toString() {
    return name + ":" + type;
  }
}

package com.revolsys.gis.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

public class Index {
  private String name;

  private boolean isUnique;

  private boolean isAscending = true;

  private List<Field> fields = new ArrayList<Field>();

  public List<Field> getFields() {
    return fields;
  }

  public String getName() {
    return name;
  }

  public boolean isIsAscending() {
    return isAscending;
  }

  public boolean isIsUnique() {
    return isUnique;
  }

  public void setIsAscending(final boolean isAscending) {
    this.isAscending = isAscending;
  }

  public void setFields(final List<Field> fields) {
    this.fields = fields;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setIsUnique(final boolean isUnique) {
    this.isUnique = isUnique;
  }

  public void addField(Field field) {
    fields.add(field);
  }
}

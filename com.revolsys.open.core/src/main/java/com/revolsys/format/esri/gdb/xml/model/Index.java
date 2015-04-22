package com.revolsys.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

public class Index {
  private String name;

  private boolean isUnique;

  private boolean isAscending = true;

  private List<Field> fields = new ArrayList<Field>();

  public void addField(final Field field) {
    this.fields.add(field);
  }

  public List<Field> getFields() {
    return this.fields;
  }

  public String getName() {
    return this.name;
  }

  public boolean isIsAscending() {
    return this.isAscending;
  }

  public boolean isIsUnique() {
    return this.isUnique;
  }

  public void setFields(final List<Field> fields) {
    this.fields = fields;
  }

  public void setIsAscending(final boolean isAscending) {
    this.isAscending = isAscending;
  }

  public void setIsUnique(final boolean isUnique) {
    this.isUnique = isUnique;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return getName();
  }
}

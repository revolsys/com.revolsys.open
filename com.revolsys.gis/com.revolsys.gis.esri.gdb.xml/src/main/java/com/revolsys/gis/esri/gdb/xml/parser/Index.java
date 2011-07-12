package com.revolsys.gis.esri.gdb.xml.parser;

import java.util.ArrayList;
import java.util.List;

public class Index {
  private String name;

  private boolean isUnique;

  private boolean isAscending;

  private List<Field> fields = new ArrayList<Field>();

  public List<Field> getFields() {
    return fields;
  }

  public String getName() {
    return name;
  }

  public boolean isAscending() {
    return isAscending;
  }

  public boolean isUnique() {
    return isUnique;
  }

  public void setAscending(final boolean isAscending) {
    this.isAscending = isAscending;
  }

  public void setFields(final List<Field> fields) {
    this.fields = fields;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setUnique(final boolean isUnique) {
    this.isUnique = isUnique;
  }
}

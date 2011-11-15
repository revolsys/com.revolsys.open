package com.revolsys.io.esri.gdb.xml.model;

import java.util.List;

public class Subtype {
  private String subtypeName;

  private int subtypeCode;

  private List<SubtypeFieldInfo> fieldInfos;

  public List<SubtypeFieldInfo> getFieldInfos() {
    return fieldInfos;
  }

  public int getSubtypeCode() {
    return subtypeCode;
  }

  public String getSubtypeName() {
    return subtypeName;
  }

  public void setFieldInfos(final List<SubtypeFieldInfo> fieldInfos) {
    this.fieldInfos = fieldInfos;
  }

  public void setSubtypeCode(final int subtypeCode) {
    this.subtypeCode = subtypeCode;
  }

  public void setSubtypeName(final String subtypeName) {
    this.subtypeName = subtypeName;
  }

}

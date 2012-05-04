package com.revolsys.io.esri.gdb.xml.model;

import java.util.List;

public class Subtype {
  private String subtypePath;

  private int subtypeCode;

  private List<SubtypeFieldInfo> fieldInfos;

  public List<SubtypeFieldInfo> getFieldInfos() {
    return fieldInfos;
  }

  public int getSubtypeCode() {
    return subtypeCode;
  }

  public String getSubtypePath() {
    return subtypePath;
  }

  public void setFieldInfos(final List<SubtypeFieldInfo> fieldInfos) {
    this.fieldInfos = fieldInfos;
  }

  public void setSubtypeCode(final int subtypeCode) {
    this.subtypeCode = subtypeCode;
  }

  public void setSubtypePath(final String subtypePath) {
    this.subtypePath = subtypePath;
  }

}

package com.revolsys.format.esri.gdb.xml.model;

import com.revolsys.format.esri.gdb.xml.model.enums.RelKeyRole;

public class RelationshipClassKey {
  private String objectKeyName;

  private String classKeyName;

  private RelKeyRole keyRole;

  public String getClassKeyName() {
    return this.classKeyName;
  }

  public RelKeyRole getKeyRole() {
    return this.keyRole;
  }

  public String getObjectKeyName() {
    return this.objectKeyName;
  }

  public void setClassKeyName(final String classKeyName) {
    this.classKeyName = classKeyName;
  }

  public void setKeyRole(final RelKeyRole keyRole) {
    this.keyRole = keyRole;
  }

  public void setObjectKeyName(final String objectKeyName) {
    this.objectKeyName = objectKeyName;
  }

}

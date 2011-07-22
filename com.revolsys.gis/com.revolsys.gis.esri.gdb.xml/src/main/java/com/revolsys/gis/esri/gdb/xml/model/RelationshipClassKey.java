package com.revolsys.gis.esri.gdb.xml.model;

import com.revolsys.gis.esri.gdb.xml.model.enums.RelKeyRole;

public class RelationshipClassKey {
  private String objectKeyName;

  private String classKeyName;

  private RelKeyRole keyRole;

  public String getObjectKeyName() {
    return objectKeyName;
  }

  public void setObjectKeyName(String objectKeyName) {
    this.objectKeyName = objectKeyName;
  }

  public String getClassKeyName() {
    return classKeyName;
  }

  public void setClassKeyName(String classKeyName) {
    this.classKeyName = classKeyName;
  }

  public RelKeyRole getKeyRole() {
    return keyRole;
  }

  public void setKeyRole(RelKeyRole keyRole) {
    this.keyRole = keyRole;
  }
  
  
}

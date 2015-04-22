package com.revolsys.format.openstreetmap.model;

public class OsmMember {
  private String type;

  private String role;

  private long ref;

  public long getRef() {
    return this.ref;
  }

  public String getRole() {
    return this.role;
  }

  public String getType() {
    return this.type;
  }

  public void setRef(final long ref) {
    this.ref = ref;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public void setType(final String type) {
    this.type = type;
  }

}

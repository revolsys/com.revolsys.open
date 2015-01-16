package com.revolsys.gis.wms.capabilities;

public class Identifier {
  private String value;

  private String authority;

  public String getAuthority() {
    return this.authority;
  }

  public String getValue() {
    return this.value;
  }

  public void setAuthority(final String authority) {
    this.authority = authority;
  }

  public void setValue(final String value) {
    this.value = value;
  }

}

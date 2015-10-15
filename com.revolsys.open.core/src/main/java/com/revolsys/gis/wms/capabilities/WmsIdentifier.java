package com.revolsys.gis.wms.capabilities;

public class WmsIdentifier {
  private String authority;

  private String value;

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

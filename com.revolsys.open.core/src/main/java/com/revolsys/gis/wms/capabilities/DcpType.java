package com.revolsys.gis.wms.capabilities;

public class DcpType {
  private final String type;

  public DcpType(final String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return this.type;
  }

}

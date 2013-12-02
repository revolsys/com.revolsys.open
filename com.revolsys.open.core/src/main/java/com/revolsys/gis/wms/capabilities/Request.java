package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

public class Request {
  private String name;

  private final List<String> formats = new ArrayList<String>();

  private final List<DcpType> dcpTypes = new ArrayList<DcpType>();

  public void addDcpType(final DcpType dcpType) {
    dcpTypes.add(dcpType);
  }

  public void addFormat(final String format) {
    formats.add(format);
  }

  public List<DcpType> getDcpTypes() {
    return dcpTypes;
  }

  public List<String> getFormats() {
    return formats;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}

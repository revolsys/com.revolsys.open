package com.revolsys.io.esri.map.rest.map;

import com.revolsys.io.esri.map.rest.AbstractMapWrapper;

public class TableDescription extends AbstractMapWrapper {
  public TableDescription() {
  }

  public Integer getId() {
    return getIntValue("id");
  }

  public String getName() {
    return getValue("name");
  }

  @Override
  public String toString() {
    return getName();
  }
}

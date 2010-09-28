package com.revolsys.gis.data.model;

import java.util.Map.Entry;

public class DataObjectEntry implements Entry<String, Object> {

  private DataObject dataObject;

  private int index;

  public DataObjectEntry(
    DataObject dataObject,
    int index) {
    this.dataObject = dataObject;
    this.index = index;
  }

  public String getKey() {
    return dataObject.getMetaData().getAttributeName(index);
  }

  public Object getValue() {
    return dataObject.getValue(index);
  }

  public Object setValue(
    Object value) {
    dataObject.setValue(index, value);
    return value;
  }
}

package com.revolsys.gis.data.model;

import java.util.Map;

import org.apache.commons.jexl.JexlContext;

public class DataObjectJexlContext implements JexlContext {

  private final DataObjectMap map = new DataObjectMap();

  public DataObjectJexlContext() {
  }

  public DataObjectJexlContext(
    final DataObject object) {
    this.map.setObject(object);
  }

  public Map getVars() {
    return map;
  }

  public void setObject(
    final DataObject object) {
    this.map.setObject(object);
  }

  public void setVars(
    final Map map) {
    this.map.putAll(map);
  }
}

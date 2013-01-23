package com.revolsys.io.carto;

public class IdSelector implements Selector {

  private String id;

  public IdSelector(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return '#' + id;
  }
}

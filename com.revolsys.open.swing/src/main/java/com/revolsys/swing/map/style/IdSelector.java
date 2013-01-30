package com.revolsys.swing.map.style;

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

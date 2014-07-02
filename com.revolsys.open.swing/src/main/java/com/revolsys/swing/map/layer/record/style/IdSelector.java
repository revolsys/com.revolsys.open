package com.revolsys.swing.map.layer.record.style;

public class IdSelector implements Selector {

  private final String id;

  public IdSelector(final String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return '#' + this.id;
  }
}

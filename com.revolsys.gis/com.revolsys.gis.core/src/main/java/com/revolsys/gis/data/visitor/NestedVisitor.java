package com.revolsys.gis.data.visitor;

import com.revolsys.collection.Visitor;

public class NestedVisitor<T> implements Visitor<T> {
  private Visitor<T> visitor;

  public NestedVisitor() {
  }

  public NestedVisitor(
    final Visitor<T> visitor) {
    this.visitor = visitor;
  }

  public Visitor<T> getVisitor() {
    return visitor;
  }

  public void setVisitor(
    final Visitor<T> visitor) {
    this.visitor = visitor;
  }

  public boolean visit(
    final T item) {
    return visitor.visit(item);
  }
}

package com.revolsys.gis.algorithm.index;

import com.revolsys.collection.Visitor;

public final class IdObjectIndexVisitor<T> implements Visitor<Integer> {
  private final IdObjectIndex<T> index;

  private final Visitor<T> visitor;

  public IdObjectIndexVisitor(final IdObjectIndex<T> index,
    final Visitor<T> visitor) {
    this.index = index;
    this.visitor = visitor;
  }

  @Override
  public boolean visit(final Integer id) {
    final T object = index.getObject(id);
    if (!visitor.visit(object)) {
      return false;
    }
    return true;
  }
}

package com.revolsys.gis.graph;

import com.revolsys.collection.Visitor;

public abstract class AbstractItemVisitor<T> implements Visitor<T>,
  com.vividsolutions.jts.index.ItemVisitor {
  @SuppressWarnings("unchecked")
  public void visitItem(Object item) {
    visit((T)item);
  }

}

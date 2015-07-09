package com.revolsys.gis.graph;

import com.revolsys.collection.Visitor;

public abstract class AbstractItemVisitor<T>
  implements Visitor<T>, com.revolsys.jts.index.ItemVisitor {
  @Override
  @SuppressWarnings("unchecked")
  public void visitItem(final Object item) {
    visit((T)item);
  }

}

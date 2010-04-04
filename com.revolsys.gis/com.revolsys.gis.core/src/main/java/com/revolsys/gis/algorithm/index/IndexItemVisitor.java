package com.revolsys.gis.algorithm.index;

import com.revolsys.gis.data.visitor.Visitor;
import com.vividsolutions.jts.index.ItemVisitor;

/**
 * A {@link ItemVisitor} implementation which uses a {@link Visitor} to visit
 * each item.
 * 
 * @author Paul Austin
 * @param <T> The type of item to visit.
 */
public class IndexItemVisitor<T> implements ItemVisitor {
  private final Visitor<T> visitor;

  public IndexItemVisitor(
    final Visitor<T> visitor) {
    this.visitor = visitor;
  }

  public void visitItem(
    final Object item) {
    visitor.visit((T)item);
  }
}

package com.revolsys.gis.graph;

import java.util.Collection;

import com.revolsys.collection.Visitor;

public class EdgeCollectionVisitor<T> implements Visitor<Edge<T>> {

  private final Collection<T> objects;

  public EdgeCollectionVisitor(final Collection<T> objects) {
    this.objects = objects;
  }

  @Override
  public boolean visit(final Edge<T> edge) {
    final T object = edge.getObject();
    objects.add(object);
    return true;
  }
}

package com.revolsys.gis.graph;

import java.util.Collection;

import com.revolsys.collection.Visitor;

public class EdgeCollectionVisitor<T> implements Visitor<Edge<T>> {

  private Collection<T> objects;

  public EdgeCollectionVisitor(Collection<T> objects) {
    this.objects = objects;
  }

  public boolean visit(Edge<T> edge) {
    T object = edge.getObject();
    objects.add(object);
    return true;
  }
}

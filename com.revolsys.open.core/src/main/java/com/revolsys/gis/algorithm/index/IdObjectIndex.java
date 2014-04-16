package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.BoundingBox;

public interface IdObjectIndex<T> extends Iterable<T> {
  public T add(final T object);

  BoundingBox getEnvelope(T object);

  int getId(T object);

  T getObject(Integer id);

  List<T> getObjects(List<Integer> ids);

  List<T> query(BoundingBox envelope);

  boolean remove(T object);

  void visit(BoundingBox envelope, Visitor<T> visitor);
}

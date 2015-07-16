package com.revolsys.gis.algorithm.index;

import java.util.List;
import java.util.function.Predicate;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.BoundingBox;

public interface EnvelopeSpatialIndex<T> {
  List<T> find(BoundingBox envelope);

  List<T> find(BoundingBox envelope, Predicate<T> filter);

  List<T> findAll();

  void put(BoundingBox envelope, T object);

  boolean remove(BoundingBox envelope, T object);

  void visit(BoundingBox envelope, Predicate<T> filter, Visitor<T> visitor);

  void visit(final BoundingBox envelope, final Visitor<T> visitor);

  void visit(final Visitor<T> visitor);
}

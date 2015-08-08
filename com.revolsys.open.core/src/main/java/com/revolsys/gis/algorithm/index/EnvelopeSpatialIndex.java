package com.revolsys.gis.algorithm.index;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.jts.geom.BoundingBox;

public interface EnvelopeSpatialIndex<T> {
  List<T> find(BoundingBox envelope);

  List<T> find(BoundingBox envelope, Predicate<T> filter);

  List<T> findAll();

  void forEach(final BoundingBox envelope, final Consumer<T> action);

  void forEach(BoundingBox envelope, Predicate<T> filter, Consumer<T> action);

  void forEach(final Consumer<T> action);

  void put(BoundingBox envelope, T object);

  boolean remove(BoundingBox envelope, T object);
}

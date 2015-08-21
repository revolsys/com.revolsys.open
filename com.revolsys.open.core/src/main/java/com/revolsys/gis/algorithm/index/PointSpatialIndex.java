package com.revolsys.gis.algorithm.index;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;

public interface PointSpatialIndex<T> extends Iterable<T> {
  List<T> find(BoundingBox envelope);

  List<T> findAll();

  void forEach(final Consumer<? super T> action, final BoundingBox envelope);

  @Override
  void forEach(final Consumer<? super T> action);

  void put(Point point, T object);

  boolean remove(Point point, T object);
}

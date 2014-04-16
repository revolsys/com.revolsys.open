package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;

public interface PointSpatialIndex<T> extends Iterable<T> {
  List<T> find(BoundingBox envelope);

  List<T> findAll();

  void put(Coordinates point, T object);

  boolean remove(Coordinates point, T object);

  void visit(final BoundingBox envelope, final Visitor<T> visitor);

  void visit(final Visitor<T> visitor);
}

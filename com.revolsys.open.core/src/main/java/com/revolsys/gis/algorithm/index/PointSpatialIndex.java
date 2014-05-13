package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;

public interface PointSpatialIndex<T> extends Iterable<T> {
  List<T> find(BoundingBox envelope);

  List<T> findAll();

  void put(Point point, T object);

  boolean remove(Point point, T object);

  void visit(final BoundingBox envelope, final Visitor<T> visitor);

  void visit(final Visitor<T> visitor);
}

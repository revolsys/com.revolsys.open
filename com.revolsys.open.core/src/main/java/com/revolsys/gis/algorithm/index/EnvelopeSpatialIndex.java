package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Envelope;

public interface EnvelopeSpatialIndex<T> {
  List<T> find(Envelope envelope);

  List<T> find(Envelope envelope, Filter<T> filter);

  List<T> findAll();

  void put(Envelope envelope, T object);

  boolean remove(Envelope envelope, T object);

  void visit(Envelope envelope, Filter<T> filter, Visitor<T> visitor);

  void visit(final Envelope envelope, final Visitor<T> visitor);

  void visit(final Visitor<T> visitor);
}

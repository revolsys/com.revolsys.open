package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.vividsolutions.jts.geom.Envelope;

public interface EnvelopeSpatialIndex<T> {
  void visit(final Envelope envelope, final Visitor<T> visitor);

  void visit(final Visitor<T> visitor);

  List<T> find(Envelope envelope);

  List<T> findAll();

  void put(Envelope envelope, T object);

  boolean remove(Envelope envelope, T object);
}

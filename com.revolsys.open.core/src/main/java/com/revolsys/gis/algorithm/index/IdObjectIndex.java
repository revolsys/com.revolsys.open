package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.vividsolutions.jts.geom.Envelope;

public interface IdObjectIndex<T> extends Iterable<T> {
  public T add(final T object);

  Envelope getEnvelope(T object);

  int getId(T object);

  T getObject(Integer id);

  List<T> getObjects(List<Integer> ids);

  List<T> query(Envelope envelope);

  boolean remove(T object);

  void visit(Envelope envelope, Visitor<T> visitor);
}

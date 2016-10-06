package com.revolsys.geometry.index;

import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;

public interface IdObjectIndex<T> extends Iterable<T> {
  public T add(final T object);

  void forEach(Consumer<? super T> action, BoundingBox envelope);

  BoundingBox getEnvelope(T object);

  int getId(T object);

  T getObject(Integer id);

  List<T> getObjects(List<Integer> ids);

  List<T> query(BoundingBox envelope);

  boolean remove(T object);
}

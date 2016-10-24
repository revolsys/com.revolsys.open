package com.revolsys.geometry.index;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;

public interface IdObjectIndex<T> extends Iterable<T> {
  public T add(final T object);

  void forEach(BoundingBox boundingBox, Consumer<? super T> action);

  void forEach(BoundingBox boundingBox, Predicate<? super T> filter, Consumer<? super T> action);

  BoundingBox getEnvelope(T object);

  int getId(T object);

  T getObject(Integer id);

  List<T> getObjects(List<Integer> ids);

  List<T> query(BoundingBox envelope);

  boolean remove(T object);
}

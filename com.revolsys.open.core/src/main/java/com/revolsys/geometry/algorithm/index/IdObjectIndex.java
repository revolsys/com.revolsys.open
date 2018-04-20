package com.revolsys.geometry.algorithm.index;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;

public interface IdObjectIndex<T> extends Iterable<T> {
  public T add(final T object);

  default void forEach(final BoundingBoxProxy boundingBox, final Consumer<? super T> action) {
    forEach(action, boundingBox.getBoundingBox());
  }

  default void forEach(final BoundingBoxProxy boundingBoxProxy, final Predicate<? super T> filter,
    final Consumer<? super T> action) {
    final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
    forEach(value -> {
      if (filter.test(value)) {
        action.accept(value);
      }
    }, boundingBox);
  }

  void forEach(Consumer<? super T> action, BoundingBox boundingBox);

  BoundingBox getEnvelope(T object);

  int getId(T object);

  T getObject(Integer id);

  List<T> getObjects(List<Integer> ids);

  List<T> query(BoundingBox envelope);

  boolean remove(T object);
}

package com.revolsys.geometry.index;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public interface BoundingBoxSpatialIndex<T> {
  default List<T> find(final BoundingBox boundingBox) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(boundingBox, visitor);
    return visitor.getList();
  }

  default List<T> find(final BoundingBox boundingBox, final Predicate<T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(boundingBox, filter, visitor);
    return visitor.getList();
  }

  default List<T> find(final double x, final double y, final Predicate<T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(x, y, filter, visitor);
    return visitor.getList();
  }

  default List<T> findAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(visitor);
    return visitor.getList();
  }

  void forEach(final BoundingBox boundingBox, final Consumer<T> action);

  void forEach(BoundingBox boundingBox, Predicate<T> filter, Consumer<T> action);

  void forEach(final Consumer<T> action);

  void forEach(double x, double y, Predicate<T> filter, Consumer<T> action);

  int getSize();

  void put(BoundingBox boundingBox, T object);

  boolean remove(BoundingBox boundingBox, T object);
}

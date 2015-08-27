package com.revolsys.geometry.algorithm.index;

import java.util.List;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public abstract class AbstractSpatialIndex<T> implements EnvelopeSpatialIndex<T> {

  @Override
  public List<T> find(final BoundingBox envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    forEach(envelope, visitor);
    return visitor.getList();
  }

  @Override
  public List<T> find(final BoundingBox envelope, final Predicate<T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    forEach(envelope, filter, visitor);
    return visitor.getList();
  }

  @Override
  public List<T> findAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    forEach(visitor);
    return visitor.getList();
  }
}

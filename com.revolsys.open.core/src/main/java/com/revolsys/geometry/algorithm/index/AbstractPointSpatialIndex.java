package com.revolsys.geometry.algorithm.index;

import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public abstract class AbstractPointSpatialIndex<T> implements PointSpatialIndex<T> {

  @Override
  public List<T> find(final BoundingBox envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(visitor, envelope);
    return visitor.getList();
  }

  @Override
  public List<T> findAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(visitor);
    return visitor.getList();
  }

  @Override
  public Iterator<T> iterator() {
    return findAll().iterator();
  }

}

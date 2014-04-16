package com.revolsys.gis.algorithm.index;

import java.util.List;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public abstract class AbstractSpatialIndex<T> implements
  EnvelopeSpatialIndex<T> {

  @Override
  public List<T> find(final BoundingBox envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(envelope, visitor);
    return visitor.getList();
  }

  @Override
  public List<T> find(final BoundingBox envelope, final Filter<T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(envelope, filter, visitor);
    return visitor.getList();
  }

  @Override
  public List<T> findAll() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(visitor);
    return visitor.getList();
  }
}

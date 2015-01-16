package com.revolsys.gis.algorithm.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public abstract class AbstractIdObjectQuadTree<T> implements IdObjectIndex<T> {

  private final QuadTree<Integer> index = new QuadTree<>();

  public void add(final Collection<Integer> ids) {
    for (final Integer id : ids) {
      final T object = getObject(id);
      add(object);
    }
  }

  @Override
  public T add(final T object) {
    final BoundingBox envelope = getEnvelope(object);
    final int id = getId(object);
    this.index.insert(envelope, id);
    return object;
  }

  @Override
  public Iterator<T> iterator() {
    return queryAll().iterator();
  }

  @Override
  public List<T> query(final BoundingBox envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(envelope, visitor);
    return visitor.getList();
  }

  public List<T> queryAll() {
    final List<Integer> ids = this.index.getAll();
    return getObjects(ids);
  }

  @Override
  public boolean remove(final T object) {
    final BoundingBox envelope = getEnvelope(object);
    final int id = getId(object);
    return this.index.remove(envelope, id);
  }

  public void removeAll(final Collection<T> objects) {
    for (final T object : objects) {
      remove(object);
    }
  }

  @Override
  public void visit(final BoundingBox envelope, final Visitor<T> visitor) {
    final IdObjectIndexItemVisitor<T> itemVisitor = new IdObjectIndexItemVisitor<T>(
        this, envelope, visitor);
    this.index.visit(envelope, itemVisitor);
  }

}

package com.revolsys.gis.algorithm.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public abstract class AbstractIdObjectQuadTree<T> extends Quadtree implements
  IdObjectIndex<T> {

  public void add(final Collection<Integer> ids) {
    for (final Integer id : ids) {
      final T object = getObject(id);
      add(object);
    }
  }

  @Override
  public T add(final T object) {
    final Envelope envelope = getEnvelope(object);
    final int id = getId(object);
    insert(envelope, id);
    return object;
  }

  @Override
  public Iterator<T> iterator() {
    return queryAll().iterator();
  }

  @Override
  public List<T> query(final Envelope envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    visit(envelope, visitor);
    return visitor.getList();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> queryAll() {
    final List<Integer> ids = super.queryAll();
    return getObjects(ids);
  }

  @Override
  public boolean remove(final T object) {
    final Envelope envelope = getEnvelope(object);
    final int id = getId(object);
    return remove(envelope, id);
  }

  public void removeAll(final Collection<T> objects) {
    for (final T object : objects) {
      remove(object);
    }
  }

  @Override
  public void visit(final Envelope envelope, final Visitor<T> visitor) {
    final IdObjectIndexItemVisitor<T> itemVisitor = new IdObjectIndexItemVisitor<T>(
      this, envelope, visitor);
    this.query(envelope, itemVisitor);
  }

}

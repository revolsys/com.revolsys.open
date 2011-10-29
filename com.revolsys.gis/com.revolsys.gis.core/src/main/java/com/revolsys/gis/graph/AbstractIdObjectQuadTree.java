package com.revolsys.gis.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.data.visitor.CreateListVisitor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public abstract class AbstractIdObjectQuadTree<T> extends Quadtree implements
  Iterable<T> {

  public void add(final Collection<Integer> ids) {
    for (final Integer id : ids) {
      final T object = getObject(id);
      add(object);
    }
  }

  public T add(final T object) {
    final Envelope envelope = getEnvelope(object);
    final int id = getId(object);
    insert(envelope, id);
    return object;
  }

  public abstract Envelope getEnvelope(T object);

  public abstract int getId(T object);

  public abstract T getObject(Integer id);

  public abstract List<T> getObjects(List<Integer> ids);

  public Iterator<T> iterator() {
    return queryAll().iterator();
  }

  @Override
  public List<T> query(final Envelope envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    query(envelope, visitor);
    return visitor.getList();
  }

  public void query(final Envelope envelope, final Visitor<T> visitor) {
    final IdObjectQuadTreeVisitor<T> itemVisitor = new IdObjectQuadTreeVisitor<T>(
      this, envelope, visitor);
    this.query(envelope, itemVisitor);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<T> queryAll() {
    final List<Integer> ids = super.queryAll();
    return getObjects(ids);
  }

  public void remove(final T object) {
    final Envelope envelope = getEnvelope(object);
    final int id = getId(object);
    remove(envelope, id);
  }

  public void removeAll(final Collection<T> objects) {
    for (final T object : objects) {
      remove(object);
    }
  }

}
